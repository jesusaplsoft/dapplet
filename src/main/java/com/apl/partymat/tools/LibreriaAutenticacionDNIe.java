package com.apl.partymat.tools;

import org.bouncycastle.ocsp.BasicOCSPResp;
import org.bouncycastle.ocsp.CertificateID;
import org.bouncycastle.ocsp.OCSPReq;
import org.bouncycastle.ocsp.OCSPReqGenerator;
import org.bouncycastle.ocsp.OCSPResp;
import org.bouncycastle.ocsp.SingleResp;

import org.bouncycastle.util.encoders.Base64;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;

import java.util.Enumeration;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import javax.security.auth.login.LoginException;
import javax.security.auth.x500.X500Principal;


/**
 * Autenticación del DNi.
 *
 * @author  jesus
 */
public class LibreriaAutenticacionDNIe {

    private static KeyStore keyStore;
    private static PrivateKey prKey;
    private static String userPIN;
    private static InputStream pkcs11;
    private static sun.security.pkcs11.SunPKCS11 sunpkcs11 = null;

    /**
     * Variables para indicar el estado del certificado.*
     */
    private static int GOOD = 0;
    private static int UNKNOWN_STATUS = 1;
    private static int REVOKED_STATUS = 2;
// private static BouncyCastleProvider jceProvider;

    /**
     * Constructor de la clase.
     */
    public LibreriaAutenticacionDNIe() {
        final String osNombre = System.getProperty("os.name");
        final String osArquitectura = System.getProperty("os.arch");
        String configuracionPKCS11 = null;

        /*
         * Configuramos el modulo PKCS#11 para el sistema operativo actual y el
         * directorio donde se encuentran los certificados intermedios de la CA
         */

        // LINUX
        if (osNombre.contains(new StringBuffer("Linux"))) {
            configuracionPKCS11 =
                "name = OpenSC\nlibrary = /usr/lib/opensc-pkcs11.so\n";
        } // WINDOWS
        else if (osNombre.contains(new StringBuffer("Windows"))) {

            if (osArquitectura.toLowerCase().contains("x86")) {
                configuracionPKCS11 =
                    "name = DNIe\nlibrary = C:\\Windows\\System32\\UsrPkcs11.dll\n";
            } else {
                configuracionPKCS11 =
                    "name = DNIe\nlibrary = C:\\Windows\\SysWoW64\\UsrPkcs11.dll\n";
            }
        } // MAC OS
        else {
            configuracionPKCS11 =
                "name = OpenSC\nlibrary = /Library/OpenSC/lib/opensc-pkcs11.so\n";
        }

        LibreriaAutenticacionDNIe.pkcs11 = new ByteArrayInputStream(
                configuracionPKCS11.getBytes());

        System.out.println("--- path config: " + configuracionPKCS11);
    }

    /**
     * Método encargado de cargar el PKCS#11 del DNIe.
     *
     * @param   pin  PIN del DNIe del usuario
     *
     * @throws  Exception  por error
     */
    public void cargarPKCS11DNIe(final String pin)
            throws Exception {
        System.out.println("Cargando PKCS11 del DNIe");

        // Inicializa el proveedor de JCE
        try {
// LibreriaAutenticacionDNIe.jceProvider =
// new org.bouncycastle.jce.provider.BouncyCastleProvider();
// LibreriaAutenticacionDNIe.jceProvider.load(
// LibreriaAutenticacionDNIe.pkcs11);

            LibreriaAutenticacionDNIe.sunpkcs11 =
                new sun.security.pkcs11.SunPKCS11(
                    LibreriaAutenticacionDNIe.pkcs11);
        } catch (final Exception ex) {
            throw new Exception(ex.getMessage()
                + ": No existe el módulo PKCS11.");
        }

        System.out.println("Añadiendo el proveedor");

        // Se añade el proveedor en la siguiente posición disponibles
// Security.addProvider(LibreriaAutenticacionDNIe.jceProvider);
        Security.addProvider(LibreriaAutenticacionDNIe.sunpkcs11);

        // Creación del objeto KeyStore con el tipo especificado.
        try {
// LibreriaAutenticacionDNIe.keyStore = KeyStore.getInstance("PKCS11",
// LibreriaAutenticacionDNIe.jceProvider);
            LibreriaAutenticacionDNIe.keyStore = KeyStore.getInstance("PKCS11",
                    LibreriaAutenticacionDNIe.sunpkcs11);
        } catch (final Exception ex) {
            throw new Exception(ex.getMessage()
                + ": No se puede instanciar el módulo PKCS11.");
        }

        System.out.println("Instancia creada");

        // Cargamos el KeyStore usando el PIN de DNIe del usuario
        try {
            LibreriaAutenticacionDNIe.keyStore.load(null, pin.toCharArray());
        } catch (final IOException ex) {
            System.out.println(ex.getMessage()
                + ": No se puede cargar el módulo PKCS11. PIN incorrecto.");
            throw ex;
        }

        // Guardamos el PIN para la autenticación
        LibreriaAutenticacionDNIe.userPIN = pin;

        System.out.println("--- pkcs11 cargado.");
    }

    /**
     * Libera almacén de certificados.
     */
    public void liberarAlmacenCertificados() {

// LibreriaAutenticacionDNIe.jceProvider.clear();
// Security.removeProvider(LibreriaAutenticacionDNIe.jceProvider
// .getName());
        try {
            LibreriaAutenticacionDNIe.sunpkcs11.logout();
            Security.removeProvider(LibreriaAutenticacionDNIe.sunpkcs11
                .getName());
            System.out.println("keystore release");
        } catch (final LoginException ex) {
            System.out.println("error: " + ex.getMessage());
        }
    }

    /**
     * Carga CSPDniE.
     *
     * <p>Si se carga el CSP del DNIe sólo hay que acceder al contenedor de
     * certificados personal de Microsoft Windows</p>
     *
     * @throws  Exception  por error.
     */
    public void cargarCSPDNIe()
            throws Exception {
        LibreriaAutenticacionDNIe.keyStore = KeyStore.getInstance("WINDOWS-MY");
        LibreriaAutenticacionDNIe.keyStore.load(null, null);
    }

    /**
     * Método encargado de buscar y seleccionar el certificado de autenticación
     * del DNIe del usuario.
     *
     * @return  El alias del certificado de autenticación
     *
     * @throws  Exception  por error
     */
    public String cargarAliasCertificadoAutenticacionDNIe()
            throws Exception {
        /*
         * Listar los alias que hay en el contenedor previamente cargado
         */
        final Enumeration e = LibreriaAutenticacionDNIe.keyStore.aliases();
        String alias = null;

        while (e.hasMoreElements()) {
            alias = e.nextElement().toString();

            // Escoge el certificado de Autenticación del DNIe
            if (alias.toLowerCase().indexOf("autenticacion") != -1) {
                final X509Certificate miCert = (X509Certificate)
                    LibreriaAutenticacionDNIe.keyStore.getCertificate(alias);
                System.out.println("Cert " + miCert.getSubjectDN().toString());
                System.out.println("Cert2 " + miCert.getSubjectDN().toString());
                System.out.println("Cert SER"
                    + miCert.getSubjectX500Principal().getName());

                final X500Principal miPral = miCert.getSubjectX500Principal();
                System.out.println("Certificado: " + miPral.getName());

                final String dn = miPral.getName();
                final LdapName ldapDN = new LdapName(dn);

                for (final Rdn rdn : ldapDN.getRdns()) {
                    System.out.println(rdn.getType() + " -> " + rdn.getValue());
                }

                return alias;
            }
        }

        return null;
    }

    /**
     * Realiza la firma del reto generado y enviado por el servidor.
     *
     * <p>En este método también se comprueba el estado de revocación del
     * certificado en cuestión mediante la verificación OCSP.</p>
     *
     * @param   data   Datos codificados en base64 correspondientes al reto del
     *                 servidor
     * @param   alias  Alias del certificado de autenticación del DNIe del
     *                 usuario
     *
     * @return  La firma del reto codificada en base64
     *
     * @throws  Exception  por error
     */
    public String autenticacionDNIe(final String data, final String alias)
            throws Exception {
        // Se descodifica el reto a firmar
        final byte[] retoAFirmar = Base64.decode(data);

        // Se comprueba el estado del certificado para verificar que es válido
        final java.security.cert.Certificate[] chain =
            LibreriaAutenticacionDNIe.keyStore.getCertificateChain(alias);

        if (this.obtenerRespuestaOCSP(alias, (X509Certificate) chain[1])
                != LibreriaAutenticacionDNIe.GOOD) {
            return null;
        }

        // Se firma el reto enviado por el servidor
        Signature jsig = null;

        jsig = Signature.getInstance("SHA1withRSA",
                LibreriaAutenticacionDNIe.keyStore.getProvider());
        LibreriaAutenticacionDNIe.prKey = (PrivateKey)
            LibreriaAutenticacionDNIe.keyStore.getKey(alias,
                LibreriaAutenticacionDNIe.userPIN.toCharArray());
        jsig.initSign(LibreriaAutenticacionDNIe.prKey);
        jsig.update(retoAFirmar);

        // Se codifica el reto firmado
        final String retoFirmado = new String(Base64.encode(jsig.sign()));

        System.out.println("--- firma realizada.");

        return retoFirmado;
    }

    /**
     * Retorna el certificado de autenticación del usuario para que el servidor
     * pueda verificar la firma del reto.
     *
     * @param   alias  Alias del certificado de autenticación del DNIe del
     *                 usuario
     *
     * @return  El certificado de autenticación del usuario
     *
     * @throws  Exception  por error
     */
    public byte[] obtenerCertificadoAutenticacionDNIe(final String alias)
            throws Exception {
        // Se necesita el certificado del usuario para comprobar la firma del
        // reto
        return LibreriaAutenticacionDNIe.keyStore.getCertificate(alias)
            .getEncoded();
    }

    /**
     * Método que comprueba el estado de revocación del certificado con el que
     * se desea hacer la autenticación.
     *
     * @param   alias     del certificado de autenticación del DNIe del usuario
     * @param   rootCert  Certificado raíz de la CA del DNIe
     *
     * @return  <ul>
     *            <li>0: Si el certificado es válido</li>
     *            <li>1: Si el estado del certificado es desconocido</li>
     *            <li>2: Si el certificado está revocado</li>
     *          </ul>
     *
     * @throws  Exception  por error
     */
    private int obtenerRespuestaOCSP(final String alias,
            final X509Certificate certificadoRaiz)
            throws Exception {
        final X509Certificate cert = (X509Certificate)
            LibreriaAutenticacionDNIe.keyStore.getCertificate(alias);

        Security.addProvider(
            new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // Se genera la petición OCSP
        final OCSPReqGenerator ocspReqGen = new OCSPReqGenerator();

        final CertificateID certid = new CertificateID(CertificateID.HASH_SHA1,
                certificadoRaiz, cert.getSerialNumber());
        ocspReqGen.addRequest(certid);

        final OCSPReq ocspReq = ocspReqGen.generate();

        final URL url = new URL("http://ocsp.dnie.es");
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestProperty("Content-Type", "application/ocsp-request");
        con.setRequestProperty("Accept", "application/ocsp-response");
        con.setDoOutput(true);

        final OutputStream out = con.getOutputStream();
        final DataOutputStream dataOut = new DataOutputStream(
                new BufferedOutputStream(out));

        dataOut.write(ocspReq.getEncoded());
        dataOut.flush();
        dataOut.close();

        final InputStream in = con.getInputStream();

        // Se obtiene la respuesta del servidor
        final BasicOCSPResp basicResp = (BasicOCSPResp)
            new OCSPResp(in).getResponseObject();

        con.disconnect();
        out.close();
        in.close();

        // Estado de los certificados a validar: GOOD, REVOKED o UKNOWN
        for (final SingleResp singResp : basicResp.getResponses()) {
            final Object status = singResp.getCertStatus();

            if (status instanceof org.bouncycastle.ocsp.UnknownStatus) {
                return LibreriaAutenticacionDNIe.UNKNOWN_STATUS;
            } else if (status instanceof org.bouncycastle.ocsp.RevokedStatus) {
                return LibreriaAutenticacionDNIe.REVOKED_STATUS;
            } else {
                return LibreriaAutenticacionDNIe.GOOD;
            }
        }

        return -1;
    }
}
