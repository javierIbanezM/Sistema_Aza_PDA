package com.azalogistics.pda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class OrdenesVerificadorTest {

    private static KeyPair par;
    private static KeyPair otroPar;

    @BeforeClass
    public static void claves() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        par = g.generateKeyPair();
        otroPar = g.generateKeyPair();
    }

    private static String firmar(String payload, KeyPair p) throws Exception {
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initSign(p.getPrivate());
        s.update(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(s.sign());
    }

    // ---- firma

    @Test
    public void firma_correcta_se_acepta() throws Exception {
        String payload = "{\"ordenes\":[{\"id\":\"1\"}]}";
        assertTrue(OrdenesVerificador.firmaValida(payload, firmar(payload, par), par.getPublic()));
    }

    @Test
    public void payload_modificado_se_rechaza() throws Exception {
        String payload = "{\"ordenes\":[{\"id\":\"1\"}]}";
        String firma = firmar(payload, par);
        assertFalse(OrdenesVerificador.firmaValida(payload + " ", firma, par.getPublic()));
        assertFalse(OrdenesVerificador.firmaValida(payload.replace("1", "2"), firma, par.getPublic()));
    }

    @Test
    public void firma_de_otra_clave_se_rechaza() throws Exception {
        String payload = "{}";
        assertFalse(OrdenesVerificador.firmaValida(payload, firmar(payload, otroPar), par.getPublic()));
    }

    @Test
    public void firma_basura_o_nula_nunca_lanza() {
        PublicKey k = par.getPublic();
        assertFalse(OrdenesVerificador.firmaValida("x", "esto no es base64!!", k));
        assertFalse(OrdenesVerificador.firmaValida("x", "", k));
        assertFalse(OrdenesVerificador.firmaValida(null, "AAAA", k));
        assertFalse(OrdenesVerificador.firmaValida("x", null, k));
        assertFalse(OrdenesVerificador.firmaValida("x", "AAAA", null));
    }

    @Test
    public void la_clave_publica_incrustada_en_la_app_es_una_clave_rsa_valida() throws Exception {
        PublicKey k = OrdenesVerificador.clavePublica(OrdenesVerificador.CLAVE_PUBLICA_B64);
        assertEquals("RSA", k.getAlgorithm());
        assertTrue(((java.security.interfaces.RSAPublicKey) k).getModulus().bitLength() >= 3072);
    }

    /**
     * Orden firmada con la clave PRIVADA real (ordenes_keys/ del proyecto Android TV Master). Si alguien cambia
     * la clave pública incrustada sin regenerar este fichero (o al revés), la app dejaría de aceptar las órdenes
     * del administrador: este test lo detecta antes de desplegar.
     */
    @Test
    public void la_clave_incrustada_valida_una_orden_firmada_con_la_clave_real() throws Exception {
        String payload = leer("orden_real.payload");
        String firma = leer("orden_real.firma");
        PublicKey k = OrdenesVerificador.clavePublica(OrdenesVerificador.CLAVE_PUBLICA_B64);
        assertTrue(OrdenesVerificador.firmaValida(payload, firma, k));
        assertFalse(OrdenesVerificador.firmaValida(payload.replace("es.tlsi.mobile", "com.aza.scanner"), firma, k));
    }

    private static String leer(String recurso) throws Exception {
        try (java.io.InputStream in = OrdenesVerificadorTest.class.getResourceAsStream("/" + recurso)) {
            if (in == null) throw new IllegalStateException("falta el recurso de test " + recurso);
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] b = new byte[4096];
            int n;
            while ((n = in.read(b)) > 0) out.write(b, 0, n);
            return out.toString("UTF-8");
        }
    }

    // ---- vigencia y destinatario

    @Test
    public void vigencia() {
        long ahora = 1_000_000;
        assertTrue(OrdenesVerificador.vigente(ahora - 10, ahora + 3600, ahora));
        assertFalse("caducada", OrdenesVerificador.vigente(ahora - 7200, ahora - 3600, ahora));
        assertFalse("emitida en el futuro", OrdenesVerificador.vigente(ahora + 3600, ahora + 7200, ahora));
        assertTrue("dentro de la tolerancia de reloj", OrdenesVerificador.vigente(ahora + 100, ahora + 3600, ahora));
        assertFalse("caduca antes de emitirse", OrdenesVerificador.vigente(ahora, ahora - 1, ahora));
    }

    @Test
    public void destinatario() {
        assertTrue(OrdenesVerificador.paraEsteTerminal("S21L04768", "S21L04768"));
        assertTrue(OrdenesVerificador.paraEsteTerminal("s21l04768", "S21L04768"));
        assertTrue(OrdenesVerificador.paraEsteTerminal("*", "S21L04768"));
        assertFalse(OrdenesVerificador.paraEsteTerminal("S26EC0772", "S21L04768"));
        assertFalse(OrdenesVerificador.paraEsteTerminal("", "S21L04768"));
        assertFalse(OrdenesVerificador.paraEsteTerminal(null, "S21L04768"));
    }

    // ---- hash

    @Test
    public void sha256_conocido() throws Exception {
        // SHA-256("abc")
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                OrdenesVerificador.sha256(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8))));
    }

    // ---- paquetes y URLs

    @Test
    public void paquetes_protegidos() {
        String yo = "com.azalogistics.pda";
        assertTrue(OrdenesVerificador.paqueteProtegido(yo, yo));
        assertTrue(OrdenesVerificador.paqueteProtegido("android", yo));
        assertTrue(OrdenesVerificador.paqueteProtegido("com.android.settings", yo));
        assertTrue(OrdenesVerificador.paqueteProtegido("com.datalogic.scan2deploy", yo));
        assertTrue(OrdenesVerificador.paqueteProtegido("com.mediatek.camera", yo));
        assertTrue(OrdenesVerificador.paqueteProtegido("com.google.android.gms", yo));
        assertTrue(OrdenesVerificador.paqueteProtegido("", yo));
        assertTrue(OrdenesVerificador.paqueteProtegido(null, yo));
        assertFalse(OrdenesVerificador.paqueteProtegido("es.tlsi.mobile", yo));
        assertFalse(OrdenesVerificador.paqueteProtegido("com.aza.scanner", yo));
    }

    @Test
    public void nombres_de_paquete_e_ids() {
        assertTrue(OrdenesVerificador.nombrePaqueteValido("com.aza.scanner"));
        assertFalse(OrdenesVerificador.nombrePaqueteValido("com.aza.scanner; rm -rf /"));
        assertFalse(OrdenesVerificador.nombrePaqueteValido("../../etc"));
        assertFalse(OrdenesVerificador.nombrePaqueteValido("sinpunto"));
        assertTrue(OrdenesVerificador.idValido("2026-10-29_001"));
        assertFalse(OrdenesVerificador.idValido("../x"));
        assertFalse(OrdenesVerificador.idValido(""));
        assertFalse(OrdenesVerificador.idValido(null));
    }

    @Test
    public void urls_permitidas() {
        Set<String> hosts = new HashSet<>();
        hosts.add("SRVAutoN8N.zar.local");
        hosts.add("192.168.2.33");
        assertTrue(OrdenesVerificador.urlPermitida("http://SRVAutoN8N.zar.local:5000/updates/a.apk", hosts));
        assertTrue(OrdenesVerificador.urlPermitida("http://srvauton8n.zar.local/a.apk", hosts));
        assertTrue(OrdenesVerificador.urlPermitida("https://192.168.2.33/a.apk", hosts));
        assertFalse("host ajeno", OrdenesVerificador.urlPermitida("http://evil.example.com/a.apk", hosts));
        assertFalse("esquema file", OrdenesVerificador.urlPermitida("file:///sdcard/a.apk", hosts));
        assertFalse("esquema ftp", OrdenesVerificador.urlPermitida("ftp://192.168.2.33/a.apk", hosts));
        assertFalse("truco de userinfo", OrdenesVerificador.urlPermitida("http://192.168.2.33@evil.com/a.apk", hosts));
        assertFalse("sin host", OrdenesVerificador.urlPermitida("http:///a.apk", hosts));
        assertFalse("basura", OrdenesVerificador.urlPermitida("no es una url", hosts));
    }
}
