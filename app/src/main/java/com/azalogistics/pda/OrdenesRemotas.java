package com.azalogistics.pda;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import com.datalogic.device.app.AppManager;
import com.datalogic.device.app.PackageInstaller;
import com.datalogic.device.app.PackageInstallerResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Canal de órdenes remotas: convierte al launcher en el "puente" para instalar, desinstalar y cerrar
 * apps en la PDA sin ADB y sin tocar la pantalla (usa el instalador silencioso del SDK de Datalogic).
 *
 * Funcionamiento:
 *  - Cada {@link #INTERVALO_S} s pregunta al servidor de actualizaciones si hay un fichero de órdenes
 *    para este terminal: {@code <base>/<NºSERIE>.json} y {@code <base>/todos.json}. Son ficheros
 *    estáticos, no hace falta ningún servicio nuevo en el servidor.
 *  - El fichero lleva el cuerpo ({@code payload}) y su firma RSA-SHA256. Solo se ejecuta si la firma
 *    coincide con la clave pública incrustada abajo; la privada nunca sale del PC de administración.
 *  - Cada orden tiene un id único y se ejecuta una sola vez (el resultado queda guardado).
 *  - Las instalaciones exigen el SHA-256 del APK y solo se descargan de servidores de confianza.
 *
 * Resultados: {@code Android/data/com.azalogistics.pda/files/ordenes_resultados.json} (legible por ADB)
 * y, para la página web, {@code AndroidBridge.getResultadosOrdenes()}.
 */
public final class OrdenesRemotas {

    private static final String TAG = "ORDENES";

    static final int INTERVALO_S = 60;
    private static final int MAX_INTENTOS = 3;
    private static final long REINTENTO_MS = 5 * 60 * 1000L;
    private static final long MAX_APK_BYTES = 300L * 1024 * 1024;
    private static final long MAX_FICHERO_ORDENES = 256 * 1024;
    private static final int TIMEOUT_SDK_S = 180;

    /** Servidores de los que se admite descargar APK (además del que sirve las órdenes). */
    private static final String[] HOSTS_CONFIANZA = {"SRVAutoN8N.zar.local", "192.168.2.33", "192.168.2.20"};

    private static final String PREFS = "ordenes_remotas";
    private static ScheduledExecutorService ejecutor;

    private OrdenesRemotas() {
    }

    /** Arranca la consulta periódica (una sola vez por proceso). Se llama desde Application.onCreate. */
    public static synchronized void iniciar(Context contexto) {
        if (ejecutor != null) return;
        final Context ctx = contexto.getApplicationContext();
        limpiarDescargasViejas(ctx);
        ejecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ordenes-remotas");
            t.setDaemon(true);
            return t;
        });
        ejecutor.scheduleWithFixedDelay(() -> {
            try {
                comprobar(ctx);
            } catch (Throwable t) {
                Log.e(TAG, "Fallo en la comprobación de órdenes", t);
            }
        }, 20, INTERVALO_S, TimeUnit.SECONDS);
        Log.i(TAG, "Órdenes remotas activas (cada " + INTERVALO_S + " s) en " + ServerConfig.getOrdenesBaseUrl(ctx));
    }

    /** Borra los APK de órdenes anteriores (p. ej. el del launcher, que no se puede borrar al actualizarse). */
    private static void limpiarDescargasViejas(Context ctx) {
        long limite = System.currentTimeMillis() - 10 * 60 * 1000L;
        File[] carpetas = {new File(ctx.getFilesDir(), "ordenes"), ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)};
        for (File dir : carpetas) {
            File[] ficheros = dir == null ? null : dir.listFiles();
            if (ficheros == null) continue;
            for (File f : ficheros) {
                if (f.getName().startsWith("orden_") && f.lastModified() < limite) //noinspection ResultOfMethodCallIgnored
                    f.delete();
            }
        }
        try { // copias antiguas en Descargas (solo las que creó esta app: MediaStore no deja tocar las ajenas)
            ctx.getContentResolver().delete(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    MediaStore.MediaColumns.DISPLAY_NAME + " LIKE 'orden\\_%' ESCAPE '\\' AND "
                            + MediaStore.MediaColumns.DATE_ADDED + " < ?",
                    new String[]{String.valueOf(limite / 1000)});
        } catch (RuntimeException e) {
            Log.d(TAG, "Sin limpieza de Descargas: " + e.getMessage());
        }
    }

    /** Copia el APK a la carpeta pública Descargas mediante MediaStore y devuelve su Uri. */
    private static Uri copiarADescargas(Context ctx, File origen) throws IOException {
        ContentResolver cr = ctx.getContentResolver();
        // Si quedó una copia de un intento anterior con el mismo nombre, MediaStore le añadiría "(1)".
        cr.delete(MediaStore.Downloads.EXTERNAL_CONTENT_URI, MediaStore.MediaColumns.DISPLAY_NAME + "=?",
                new String[]{origen.getName()});
        ContentValues v = new ContentValues();
        v.put(MediaStore.MediaColumns.DISPLAY_NAME, origen.getName());
        v.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.android.package-archive");
        v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        Uri uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
        if (uri == null) throw new IOException("MediaStore no creó el fichero");
        try (InputStream in = new java.io.FileInputStream(origen); OutputStream out = cr.openOutputStream(uri)) {
            if (out == null) throw new IOException("no se pudo abrir el fichero de Descargas");
            byte[] b = new byte[64 * 1024];
            int n;
            while ((n = in.read(b)) > 0) out.write(b, 0, n);
        }
        return uri;
    }

    /** Ruta real (/storage/emulated/0/Download/...) de un fichero creado con MediaStore. */
    private static String rutaDe(Context ctx, Uri uri) throws IOException {
        try (Cursor c = ctx.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null)) {
            if (c != null && c.moveToFirst() && c.getString(0) != null) return c.getString(0);
        }
        throw new IOException("MediaStore no devolvió la ruta del fichero");
    }

    // ------------------------------------------------------------------ consulta

    static void comprobar(Context ctx) {
        if (!hayRed(ctx)) return;
        String serie = DeviceIdUtil.obtenerNumeroSerie(ctx);
        String base = ServerConfig.getOrdenesBaseUrl(ctx);
        PublicKey clave;
        try {
            clave = OrdenesVerificador.clavePublica(OrdenesVerificador.CLAVE_PUBLICA_B64);
        } catch (Exception e) {
            Log.e(TAG, "Clave pública no válida", e);
            return;
        }
        for (String nombre : new String[]{serie, "todos"}) {
            if (nombre == null || nombre.isEmpty()) continue;
            String cuerpo;
            try {
                cuerpo = descargarTexto(base + "/" + URLEncoder.encode(nombre, "UTF-8") + ".json");
            } catch (IOException e) {
                Log.d(TAG, "Sin acceso a las órdenes (" + nombre + "): " + e.getMessage());
                continue;
            }
            if (cuerpo == null) continue; // 404: no hay órdenes para este destino
            procesarFichero(ctx, cuerpo, serie, clave);
        }
    }

    private static void procesarFichero(Context ctx, String cuerpo, String serie, PublicKey clave) {
        try {
            JSONObject fichero = new JSONObject(cuerpo);
            String payload = fichero.getString("payload");
            if (!OrdenesVerificador.firmaValida(payload, fichero.getString("firma"), clave)) {
                Log.w(TAG, "Fichero de órdenes con FIRMA NO VÁLIDA: se ignora");
                return;
            }
            JSONObject p = new JSONObject(payload);
            long ahora = System.currentTimeMillis() / 1000;
            if (!OrdenesVerificador.vigente(p.getLong("emitido"), p.getLong("caduca"), ahora)) {
                Log.w(TAG, "Fichero de órdenes caducado o con fecha incorrecta: se ignora");
                return;
            }
            if (!OrdenesVerificador.paraEsteTerminal(p.optString("destino"), serie)) {
                Log.w(TAG, "Fichero de órdenes para otro destino (" + p.optString("destino") + "): se ignora");
                return;
            }
            JSONArray ordenes = p.getJSONArray("ordenes");
            for (int i = 0; i < ordenes.length(); i++) {
                JSONObject orden = ordenes.getJSONObject(i);
                String id = orden.optString("id");
                if (!OrdenesVerificador.idValido(id)) {
                    Log.w(TAG, "Orden con id no válido: se ignora");
                    continue;
                }
                if (!toca(ctx, id)) continue;
                Resultado r = ejecutar(ctx, id, orden);
                registrar(ctx, id, orden.optString("tipo"), r);
            }
        } catch (Exception e) {
            Log.w(TAG, "Fichero de órdenes mal formado: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ ejecución

    private static final class Resultado {
        final boolean ok;
        final String detalle;

        Resultado(boolean ok, String detalle) {
            this.ok = ok;
            this.detalle = detalle;
        }
    }

    private static Resultado ok(String detalle) {
        return new Resultado(true, detalle);
    }

    private static Resultado error(String detalle) {
        return new Resultado(false, detalle);
    }

    private static Resultado ejecutar(Context ctx, String id, JSONObject orden) {
        String tipo = orden.optString("tipo");
        String paquete = orden.optString("paquete", "");
        Log.i(TAG, "Ejecutando orden " + id + ": " + tipo + " " + paquete);
        try {
            switch (tipo) {
                case "instalar":
                    return instalar(ctx, id, orden, false);
                case "actualizar_launcher":
                    return instalar(ctx, id, orden, true);
                case "desinstalar":
                    return desinstalar(ctx, paquete);
                case "cerrar":
                    return cerrar(ctx, paquete);
                default:
                    return error("tipo de orden desconocido: " + tipo);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error ejecutando la orden " + id, t);
            return error(t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }

    private static Resultado instalar(Context ctx, String id, JSONObject orden, boolean launcher) throws Exception {
        String url = orden.optString("url");
        String sha = orden.optString("sha256").toLowerCase(java.util.Locale.ROOT);
        String esperado = orden.optString("paquete", "");
        if (!sha.matches("[0-9a-f]{64}")) return error("falta un sha256 válido");
        if (!OrdenesVerificador.urlPermitida(url, hostsPermitidos(ctx))) return error("URL no permitida: " + url);

        // Se descarga primero en la memoria interna de la app: el SDK de Datalogic >= 1.33 sabe instalar desde
        // ahí. Si el instalador no pudiera leerla, más abajo se reintenta desde el almacenamiento externo.
        File dir = new File(ctx.getFilesDir(), "ordenes");
        if (!dir.isDirectory() && !dir.mkdirs()) return error("no se pudo crear " + dir);
        File apk = new File(dir, "orden_" + id + ".apk");
        try {
            descargarArchivo(url, apk);
            String real = OrdenesVerificador.sha256(apk);
            if (!real.equals(sha)) return error("el SHA-256 del APK no coincide (" + real + ")");

            PackageManager pm = ctx.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apk.getAbsolutePath(), 0);
            if (info == null) return error("el fichero descargado no es un APK válido");
            String paqueteApk = info.packageName;
            if (!esperado.isEmpty() && !esperado.equals(paqueteApk)) {
                return error("el APK es de " + paqueteApk + " y la orden decía " + esperado);
            }
            boolean esPropio = paqueteApk.equals(ctx.getPackageName());
            if (launcher != esPropio) {
                return error(launcher ? "actualizar_launcher exige el APK del propio launcher"
                        : "para actualizar el launcher usa actualizar_launcher");
            }
            if (!esPropio && OrdenesVerificador.paqueteProtegido(paqueteApk, ctx.getPackageName())) {
                return error("paquete protegido: " + paqueteApk);
            }
            if (!esPropio && esDeSistema(pm, paqueteApk)) return error(paqueteApk + " es una app del sistema");

            if (esPropio) {
                long actual = pm.getPackageInfo(ctx.getPackageName(), 0).getLongVersionCode();
                if (info.getLongVersionCode() < actual) {
                    return error("el APK del launcher es más antiguo (" + info.getLongVersionCode() + " < " + actual + ")");
                }
                // El proceso muere durante la actualización: el resultado se guarda antes.
                registrar(ctx, id, "actualizar_launcher", ok("actualizando el launcher a build " + info.getLongVersionCode()));
                Resultado u = sdkActualizarLauncher(ctx, apk.getAbsolutePath());
                if (u.ok) return u;
                try { // mismo respaldo que en instalar(): algunos firmwares solo leen Descargas
                    Resultado u2 = sdkActualizarLauncher(ctx, rutaDe(ctx, copiarADescargas(ctx, apk)));
                    return u2.ok ? u2 : error(u.detalle + " | reintento desde Descargas: " + u2.detalle);
                } catch (IOException e) {
                    return error(u.detalle + " | no se pudo preparar Descargas: " + e.getMessage());
                }
            }
            Resultado r = sdkInstalar(ctx, apk.getAbsolutePath(), paqueteApk);
            if (r.ok) return r;
            // El instalador de Datalogic de algunos firmwares (comprobado en Memor 10, Android 10) solo lee
            // carpetas públicas: se reintenta desde Descargas, escrita con MediaStore (válido con
            // almacenamiento restringido) y borrada al terminar.
            Uri publico = null;
            try {
                publico = copiarADescargas(ctx, apk);
                Resultado r2 = sdkInstalar(ctx, rutaDe(ctx, publico), paqueteApk);
                return r2.ok ? r2 : error(r.detalle + " | reintento desde Descargas: " + r2.detalle);
            } catch (IOException e) {
                return error(r.detalle + " | no se pudo preparar Descargas: " + e.getMessage());
            } finally {
                if (publico != null) ctx.getContentResolver().delete(publico, null, null);
            }
        } finally {
            if (!launcher) //noinspection ResultOfMethodCallIgnored
                apk.delete();
        }
    }

    private static Resultado desinstalar(Context ctx, String paquete) throws Exception {
        if (!OrdenesVerificador.nombrePaqueteValido(paquete)) return error("nombre de paquete no válido");
        if (OrdenesVerificador.paqueteProtegido(paquete, ctx.getPackageName())) return error("paquete protegido: " + paquete);
        PackageManager pm = ctx.getPackageManager();
        if (!instalado(pm, paquete)) return ok("no estaba instalada");
        if (esDeSistema(pm, paquete)) return error(paquete + " es una app del sistema");

        final CountDownLatch latch = new CountDownLatch(1);
        final int[] codigo = {Integer.MIN_VALUE};
        final String[] detalle = {""};
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                int rc = new PackageInstaller(ctx).uninstall(paquete, resultados -> {
                    codigo[0] = resumen(resultados, detalle);
                    latch.countDown();
                });
                if (rc != 0) {
                    codigo[0] = rc;
                    detalle[0] = "rc=" + rc;
                    latch.countDown();
                }
            } catch (Throwable t) {
                codigo[0] = -1;
                detalle[0] = t.toString();
                latch.countDown();
            }
        });
        if (!latch.await(TIMEOUT_SDK_S, TimeUnit.SECONDS)) return error("tiempo de espera agotado");
        if (codigo[0] != 0) return error("el instalador de Datalogic devolvió " + codigo[0] + " " + detalle[0]);
        return instalado(pm, paquete) ? error("sigue instalada tras desinstalar") : ok("desinstalada");
    }

    private static Resultado cerrar(Context ctx, String paquete) {
        if (!OrdenesVerificador.nombrePaqueteValido(paquete)) return error("nombre de paquete no válido");
        if (OrdenesVerificador.paqueteProtegido(paquete, ctx.getPackageName())) return error("paquete protegido: " + paquete);
        int rc = new AppManager(ctx).forceStopPackage(paquete);
        return rc == 0 ? ok("cerrada") : error("forceStopPackage devolvió " + rc);
    }

    private static Resultado sdkInstalar(Context ctx, String ruta, String paquete) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final int[] codigo = {Integer.MIN_VALUE};
        final String[] detalle = {""};
        // El SDK avisa por el hilo principal: se llama desde ahí y se espera aquí, en el hilo de trabajo.
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                int rc = new PackageInstaller(ctx).install(ruta, true, resultados -> {
                    codigo[0] = resumen(resultados, detalle);
                    latch.countDown();
                });
                if (rc != 0) {
                    codigo[0] = rc;
                    detalle[0] = "rc=" + rc;
                    latch.countDown();
                }
            } catch (Throwable t) {
                codigo[0] = -1;
                detalle[0] = t.toString();
                latch.countDown();
            }
        });
        if (!latch.await(TIMEOUT_SDK_S, TimeUnit.SECONDS)) return error("tiempo de espera agotado");
        if (codigo[0] != 0) return error("el instalador de Datalogic devolvió " + codigo[0] + " " + detalle[0]);
        return instalado(ctx.getPackageManager(), paquete) ? ok("instalada") : error("no aparece instalada tras el instalador");
    }

    private static Resultado sdkActualizarLauncher(Context ctx, String ruta) {
        final int[] rc = {Integer.MIN_VALUE};
        final CountDownLatch latch = new CountDownLatch(1);
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                rc[0] = new PackageInstaller(ctx).selfUpgrade(ruta);
            } catch (Throwable t) {
                Log.e(TAG, "selfUpgrade", t);
                rc[0] = -1;
            }
            latch.countDown();
        });
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return rc[0] == 0 ? ok("actualizando el launcher") : error("selfUpgrade devolvió " + rc[0]);
    }

    /** 0 si todos los resultados son correctos; si no, el primer código de error. */
    private static int resumen(java.util.List<PackageInstallerResult> resultados, String[] detalle) {
        int codigo = 0;
        for (PackageInstallerResult r : resultados) {
            if (r.result != 0 && codigo == 0) codigo = r.result;
            detalle[0] += r.event + ":" + r.data + "=" + r.result + " ";
        }
        return codigo;
    }

    private static boolean instalado(PackageManager pm, String paquete) {
        try {
            pm.getPackageInfo(paquete, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static boolean esDeSistema(PackageManager pm, String paquete) {
        try {
            ApplicationInfo ai = pm.getApplicationInfo(paquete, 0);
            return (ai.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static Set<String> hostsPermitidos(Context ctx) {
        Set<String> hosts = new HashSet<>();
        for (String h : HOSTS_CONFIANZA) hosts.add(h);
        try {
            String host = new java.net.URI(ServerConfig.getOrdenesBaseUrl(ctx)).getHost();
            if (host != null) hosts.add(host);
        } catch (java.net.URISyntaxException ignored) {
            // se queda con la lista fija
        }
        return hosts;
    }

    // ------------------------------------------------------------------ estado y resultados

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** ¿Hay que ejecutar esta orden ahora? No si ya salió bien, ni si agotó los intentos o falló hace poco. */
    private static boolean toca(Context ctx, String id) {
        String guardado = prefs(ctx).getString("o_" + id, null);
        if (guardado == null) return true;
        try {
            JSONObject r = new JSONObject(guardado);
            if (r.optBoolean("ok")) return false;
            if (r.optInt("intentos") >= MAX_INTENTOS) return false;
            return System.currentTimeMillis() - r.optLong("cuando") >= REINTENTO_MS;
        } catch (Exception e) {
            return true;
        }
    }

    private static void registrar(Context ctx, String id, String tipo, Resultado r) {
        try {
            int intentos = 1;
            String previo = prefs(ctx).getString("o_" + id, null);
            if (previo != null) intentos = new JSONObject(previo).optInt("intentos") + 1;
            JSONObject j = new JSONObject();
            j.put("tipo", tipo);
            j.put("ok", r.ok);
            j.put("detalle", r.detalle);
            j.put("intentos", intentos);
            j.put("cuando", System.currentTimeMillis());
            prefs(ctx).edit().putString("o_" + id, j.toString()).commit();
            Log.i(TAG, "Orden " + id + (r.ok ? " OK: " : " ERROR: ") + r.detalle);
            escribirResultados(ctx);
        } catch (Exception e) {
            Log.e(TAG, "No se pudo registrar el resultado de " + id, e);
        }
    }

    /** Todos los resultados guardados, como JSON {"id": {tipo, ok, detalle, intentos, cuando}}. */
    public static String resultadosJson(Context ctx) {
        JSONObject todo = new JSONObject();
        try {
            for (Map.Entry<String, ?> e : prefs(ctx).getAll().entrySet()) {
                if (e.getKey().startsWith("o_") && e.getValue() instanceof String) {
                    todo.put(e.getKey().substring(2), new JSONObject((String) e.getValue()));
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "No se pudieron leer los resultados", e);
        }
        return todo.toString();
    }

    private static void escribirResultados(Context ctx) {
        File dir = ctx.getExternalFilesDir(null);
        if (dir == null) return;
        try (OutputStream out = new FileOutputStream(new File(dir, "ordenes_resultados.json"))) {
            out.write(resultadosJson(ctx).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.w(TAG, "No se pudo escribir ordenes_resultados.json", e);
        }
    }

    // ------------------------------------------------------------------ red

    private static boolean hayRed(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null || cm.getActiveNetwork() == null) return false;
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    /** GET de texto; null si el servidor responde 404 (sin órdenes). */
    private static String descargarTexto(String direccion) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(direccion).openConnection();
        c.setConnectTimeout(6000);
        c.setReadTimeout(8000);
        c.setUseCaches(false);
        c.setRequestProperty("Cache-Control", "no-cache");
        try {
            int codigo = c.getResponseCode();
            if (codigo == 404) return null;
            if (codigo != 200) throw new IOException("HTTP " + codigo);
            try (InputStream in = c.getInputStream()) {
                java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
                byte[] b = new byte[8192];
                int n;
                while ((n = in.read(b)) > 0) {
                    buf.write(b, 0, n);
                    if (buf.size() > MAX_FICHERO_ORDENES) throw new IOException("fichero de órdenes demasiado grande");
                }
                return buf.toString("UTF-8");
            }
        } finally {
            c.disconnect();
        }
    }

    private static void descargarArchivo(String direccion, File destino) throws IOException {
        File parcial = new File(destino.getParentFile(), destino.getName() + ".part");
        HttpURLConnection c = (HttpURLConnection) new URL(direccion).openConnection();
        c.setConnectTimeout(10000);
        c.setReadTimeout(30000);
        try {
            if (c.getResponseCode() != 200) throw new IOException("HTTP " + c.getResponseCode() + " al descargar el APK");
            long total = 0;
            try (InputStream in = c.getInputStream(); OutputStream out = new FileOutputStream(parcial)) {
                byte[] b = new byte[64 * 1024];
                int n;
                while ((n = in.read(b)) > 0) {
                    total += n;
                    if (total > MAX_APK_BYTES) throw new IOException("APK demasiado grande");
                    out.write(b, 0, n);
                }
            }
            if (destino.exists() && !destino.delete()) throw new IOException("no se pudo reemplazar " + destino);
            if (!parcial.renameTo(destino)) throw new IOException("no se pudo guardar el APK descargado");
        } finally {
            c.disconnect();
            if (parcial.exists()) //noinspection ResultOfMethodCallIgnored
                parcial.delete();
        }
    }
}
