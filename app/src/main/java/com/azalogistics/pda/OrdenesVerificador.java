package com.azalogistics.pda;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Locale;

/**
 * Comprobaciones de seguridad de las órdenes remotas, sin dependencias de Android para poder
 * probarlas con tests de JVM (ver OrdenesVerificadorTest). Aquí vive todo lo que decide si una
 * orden puede ejecutarse: firma RSA, vigencia y destinatario.
 */
public final class OrdenesVerificador {

    /**
     * Clave pública RSA (X.509 DER, base64) con la que debe estar firmado todo fichero de órdenes. La
     * privada nunca sale del PC de administración (Android TV Master: ordenes_keys/). Si hay que
     * cambiarla, se genera otro par y se publica una versión nueva del launcher con esta constante.
     */
    static final String CLAVE_PUBLICA_B64 =
            "MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEArBHQXKXdu0+IsguXJRpG1OqQdBlGXjWMxYB/5/QzSeuY"
            + "/3sfwhOmOS4FFB4oIpW5aCmKX5LQ+u17GteTlQ37t0Gd22ZvaCVS9rck5s36VKYpdPtmLYGN2rCAMUblQXMi03wP"
            + "qSTrRwij8CIp42ptBTwlHOkspgMaTCMF9S+maR/er0rCoXQbpFm3H7ahBRRqJFx7UJo55EUFzIgmuAaX+c/oDnkt"
            + "JYpvdaLpcf9FDsn/w4g3xy61i6tfUt7YzsW5zFNcDzWEyUskzvrE+riEflNXl7/TduWh9v5+cBpYd4Z2ciEpxcA/"
            + "YmaWmbzrCFoHf+fQ18aGeL72DDk0Md7dz5+hxZkjv3M8WeiXqBvVeLXjqTZtPPiS9tS4I/qHFT9R8cos/+kpD+1G"
            + "BvP6/uSE3ghCYf3wDTwZMMol7OFbWSZlAw/xPWEQGHfpiegfK8uJeGn1e5kKlLl8J6RpXt8I6FARIzC6eIVChdCK"
            + "Z7RvF7jPhkF5Z9NzfFnrin0opjfPAgMBAAE=";

    /** Margen para relojes desajustados entre quien firma y el terminal. */
    static final long TOLERANCIA_RELOJ_S = 300;

    private OrdenesVerificador() {
    }

    /** Clave pública X.509 (DER) en base64: la que está incrustada en la app. */
    public static PublicKey clavePublica(String base64Der) throws GeneralSecurityException {
        byte[] der = Base64.getDecoder().decode(base64Der.replaceAll("\\s", ""));
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
    }

    /** ¿{@code firmaB64} es la firma RSA-SHA256 (PKCS#1 v1.5) de {@code payload}? Nunca lanza: ante cualquier fallo, false. */
    public static boolean firmaValida(String payload, String firmaB64, PublicKey clave) {
        if (payload == null || firmaB64 == null || clave == null) return false;
        try {
            Signature s = Signature.getInstance("SHA256withRSA");
            s.initVerify(clave);
            s.update(payload.getBytes(StandardCharsets.UTF_8));
            return s.verify(Base64.getDecoder().decode(firmaB64.replaceAll("\\s", "")));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Vigencia: ni caducada ni emitida "en el futuro" (más allá de la tolerancia de reloj). */
    public static boolean vigente(long emitidoS, long caducaS, long ahoraS) {
        return caducaS > ahoraS && emitidoS <= ahoraS + TOLERANCIA_RELOJ_S && caducaS > emitidoS;
    }

    /** El fichero firmado va para este terminal (su nº de serie) o para todos ("*"). */
    public static boolean paraEsteTerminal(String destino, String serie) {
        if (destino == null || destino.isEmpty()) return false;
        return "*".equals(destino) || (serie != null && destino.equalsIgnoreCase(serie));
    }

    /** SHA-256 en hexadecimal minúscula de un fichero. */
    public static String sha256(File f) throws IOException {
        try (InputStream in = new FileInputStream(f)) {
            return sha256(in);
        }
    }

    public static String sha256(InputStream in) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
            return hex(md.digest());
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    public static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format(Locale.ROOT, "%02x", b));
        return sb.toString();
    }

    /**
     * Paquetes que ninguna orden puede tocar: la propia app, el sistema Android y las apps de
     * fabricante (Datalogic/MediaTek) de las que depende el terminal. Es una lista de seguridad por
     * si alguna vez se firma una orden equivocada.
     */
    public static boolean paqueteProtegido(String paquete, String propio) {
        if (paquete == null || paquete.isEmpty()) return true;
        if (paquete.equals(propio)) return true;
        String p = paquete.toLowerCase(Locale.ROOT);
        return p.equals("android")
                || p.startsWith("com.android.")
                || p.startsWith("com.datalogic.")
                || p.startsWith("com.mediatek.")
                || p.startsWith("com.google.android.gms")
                || p.startsWith("com.google.android.gsf")
                || p.equals("com.android.vending");
    }

    /** Nombre de paquete Android válido (evita rutas o comandos disfrazados). */
    public static boolean nombrePaqueteValido(String paquete) {
        return paquete != null && paquete.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+");
    }

    /** Identificador de orden usable como nombre de fichero y clave. */
    public static boolean idValido(String id) {
        return id != null && id.matches("[A-Za-z0-9._-]{1,64}");
    }

    /** Solo http/https y solo servidores de la lista de confianza. */
    public static boolean urlPermitida(String url, java.util.Set<String> hostsPermitidos) {
        try {
            java.net.URI u = new java.net.URI(url);
            String esquema = u.getScheme();
            String host = u.getHost();
            if (host == null || esquema == null) return false;
            if (!esquema.equalsIgnoreCase("http") && !esquema.equalsIgnoreCase("https")) return false;
            for (String h : hostsPermitidos) {
                if (h.equalsIgnoreCase(host)) return true;
            }
            return false;
        } catch (java.net.URISyntaxException e) {
            return false;
        }
    }
}
