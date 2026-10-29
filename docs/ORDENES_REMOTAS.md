# Órdenes remotas (instalar / desinstalar / cerrar apps sin ADB)

Desde la versión **1.6.0** el launcher (`com.azalogistics.pda`) hace de puente: recoge órdenes firmadas del servidor de
actualizaciones y las ejecuta con el instalador **silencioso** del SDK de Datalogic. No necesita ADB, no pide confirmación en
pantalla y no depende de la IP de la PDA (es la PDA la que pregunta).

Solo funciona en **terminales Datalogic** (usa `com.datalogic.device.app.PackageInstaller` y `AppManager`). Verificado en una
**Memor 10 (Android 10)**; la Memor 11 usa el mismo SDK pero no se ha probado.

## Cómo funciona

1. Cada 60 s (desde `App.onCreate`, mientras el proceso vive) el launcher pide, con HTTP, estos ficheros:
   - `<base>/<NºSERIE>.json`  → órdenes para ese terminal
   - `<base>/todos.json`      → órdenes para todos los terminales (`destino: "*"`)

   `<base>` es `http://SRVAutoN8N.zar.local:5000/updates/ordenes` (`ServerConfig.getOrdenesBaseUrl`). Un 404 significa «sin
   órdenes». Son ficheros estáticos: **no hace falta ningún servicio nuevo**; basta con dejarlos en la carpeta `updates` que ya
   sirve `versionPDA.json`.
2. El fichero tiene la forma `{"payload": "<texto JSON>", "firma": "<base64>"}`. La firma es RSA-SHA256 (PKCS#1 v1.5) del
   texto `payload`. **Solo se ejecuta si la firma coincide con la clave pública incrustada en la app**
   (`OrdenesVerificador.CLAVE_PUBLICA_B64`).
3. Dentro del `payload`: `emitido`, `caduca` (segundos Unix), `destino` (nº de serie o `*`) y la lista `ordenes`.
4. Cada orden tiene un `id` único y se ejecuta **una sola vez**; el resultado queda guardado. Si falla, se reintenta
   hasta 3 veces con 5 min entre intentos.

### Tipos de orden

| `tipo` | Campos | Qué hace |
|---|---|---|
| `instalar` | `url`, `sha256`, `paquete` (opcional) | Descarga el APK, comprueba el SHA-256, e instala o actualiza en silencio |
| `desinstalar` | `paquete` | Desinstala una app de usuario (si ya no estaba, devuelve OK) |
| `cerrar` | `paquete` | Fuerza el cierre de la app |
| `actualizar_launcher` | `url`, `sha256` | Actualiza el propio launcher (`selfUpgrade`); exige un APK del mismo paquete y no más antiguo |

## Seguridad

- **Firma obligatoria** con la clave privada del administrador (`ordenes_keys/ordenes_privada.pem` en el proyecto *Android TV
  Master*). Es lo único que hay que proteger: quien la tenga puede instalar y desinstalar apps en todas las PDA. **No está en
  este repositorio** ni debe subirse al servidor.
- **Caducidad** (24 h por defecto): un fichero viejo que alguien vuelva a copiar al servidor deja de valer.
- **Destinatario** firmado: copiar el fichero de un terminal al de otro no sirve.
- **SHA-256 obligatorio** del APK y descarga solo de servidores de confianza (`SRVAutoN8N.zar.local`, `192.168.2.33`,
  `192.168.2.20` y el host de `<base>`); solo `http`/`https`.
- **Paquetes protegidos** que ninguna orden puede tocar: la propia app, `android`, `com.android.*`, `com.datalogic.*`,
  `com.mediatek.*`, Google Play Services y Play Store, y cualquier app del sistema.
- El launcher solo se actualiza con `actualizar_launcher` (no con `instalar`).
- El tráfico va por HTTP (como el resto de la app): la integridad la garantizan la **firma** y el **hash**, no el transporte.

## Resultados

- `Android/data/com.azalogistics.pda/files/ordenes_resultados.json` (legible por ADB: `python atv.py orden-estado "PDA 99"`).
- Página web: `AndroidBridge.getResultadosOrdenes()` devuelve el mismo JSON.
- Log: `adb logcat -s ORDENES`.

## Uso desde el PC (Android TV Master)

```
python atv.py orden-instalar "PDA 99,PDA 100" C:\apks\mi_app.apk --paquete com.ejemplo.app
python atv.py orden-desinstalar "PDA 99" com.ejemplo.app
python atv.py orden-cerrar all com.ejemplo.app
python atv.py orden-estado "PDA 99"
```
Publica en el servidor de actualizaciones (`--servidor`, `--url-base` para otro servidor). `--solo-mostrar` enseña qué
publicaría sin escribir nada. Un fichero por terminal: publicar una orden nueva **sustituye** las pendientes de ese terminal
(las ya ejecutadas no se repiten, porque su `id` queda registrado en la PDA).

## Detalles técnicos y límites verificados

- El instalador de Datalogic de la Memor 10 **no lee** ni la memoria interna ni `Android/data` de la app: solo carpetas
  públicas. Por eso la app descarga el APK a su memoria interna, intenta instalar y, si falla, lo copia a `Descargas` con
  **MediaStore** (válido con almacenamiento restringido), reintenta y lo borra.
- `PackageInstaller` avisa por el hilo principal; el trabajo se hace en un hilo aparte que espera con un `CountDownLatch`.
- Cambiar la clave: generar otro par, sustituir `CLAVE_PUBLICA_B64` y `app/src/test/resources/orden_real.*` (los regenera
  `atvmaster/ordenes.py`) y publicar una versión nueva del launcher. Mientras no se actualice, las PDA viejas rechazan lo
  firmado con la clave nueva.
- Tests: `OrdenesVerificadorTest` (firma, caducidad, destinatario, hash, paquetes protegidos, URLs y una orden firmada con la
  clave real). **Gradle no puede lanzar los tests en rutas con «ñ» (`C:\Users\jibañez`)**: se ejecutan con JUnit directamente
  (ver «Cómo probar» más abajo).

## Cómo probar sin tocar producción

1. Servidor local: `python -m http.server 8099 --directory <carpeta>` con `ordenes/<SERIE>.json` y `apps/…`.
2. PDA por USB: `adb reverse tcp:8099 tcp:8099`, y apuntar el launcher a `http://127.0.0.1:8099/ordenes` escribiendo
   `shared_prefs/server_config.xml` (clave `ordenesUrl`) con `adb shell run-as com.azalogistics.pda` (build de depuración).
3. Publicar con `python atv.py orden-… --servidor <carpeta> --url-base http://127.0.0.1:8099`.
4. Al terminar, borrar `server_config.xml` y `ordenes_remotas.xml` para volver a la URL de producción.

Tests de JVM sin Gradle:

```
javac -cp junit-4.13.2.jar;hamcrest-core-1.3.jar -d out app/src/main/java/com/azalogistics/pda/OrdenesVerificador.java app/src/test/java/com/azalogistics/pda/OrdenesVerificadorTest.java
java  -cp out;app/src/test/resources;junit-4.13.2.jar;hamcrest-core-1.3.jar org.junit.runner.JUnitCore com.azalogistics.pda.OrdenesVerificadorTest
```
