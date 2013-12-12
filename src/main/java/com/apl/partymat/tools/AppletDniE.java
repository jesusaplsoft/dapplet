package com.apl.partymat.tools;

import java.applet.Applet;

import java.awt.Graphics;
import java.awt.HeadlessException;

import java.io.IOException;

import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.cert.CertificateException;


/**
 * Obtener dni electrónico conectado a una SmartCard.
 *
 * <p>Para ello, pide el PIN y accede al dni electrónico que se conecte a la
 * SmartCard.</p>
 *
 * @author  jesus
 */
public class AppletDniE
    extends Applet {

    private static final long serialVersionUID = 7217692140248075908L;

    // pin pasado desde html
    private String pin = "";
    // mensajes de error que se van produciendo
    // Puede ser OK - dni Apellidos y nombre
    private String msgError;
    private boolean first = true;
    // Parámetros pasados
    int height; // altura del panel de contenido del applet
    int width; // anchura del panel de contenido del applet
    int maxHeight; // Máx. altura del panel de contenido del applet
    int maxWidth; // Máx. anchura del panel de contenido del applet

    private LibreriaAutenticacionDNIe libAutDNIe;

    private String firma;
    private String certificado;
    private String reto;

    /**
     * Constructor del formulario.
     *
     * @throws  HeadlessException  si no hay ratón, teclado o pantalla
     */
    public AppletDniE()
            throws HeadlessException {
    }

    /**
     * Recibe pin desde html.
     *
     * @param  pin  a procesar
     */
    public void setPin(final String pin) {

        System.err.println("setPin");
// if (!this.msgError.equals("")) {
//
// if (this.first) {
// ;
// } else {
        this.pin = pin;
        this.login();
        this.repaint();
// }
// }
//
// this.first = false;
    }

    public String getResult() {
        return this.msgError;
    }

    /**
     * Llamado por el navegador para informar al applet de que ha sido cargado
     * en el sistema.
     *
     * <p>Se llama siempre antes de llamar a <code>start()</code> por primera
     * vez.</p>
     */
    @Override
    public void init() {
        super.init();
        System.err.println("init");

        try {
            this.libAutDNIe = new LibreriaAutenticacionDNIe();
        } catch (final Exception ex) {
            this.msgError = ex.getLocalizedMessage();
        }

        this.loadAppletParameters();
    }

    /**
     * Es llamado por el navegador para informar a este applet de que debería de
     * empezar su ejecución.
     *
     * <p>Se llama tras el método <code>init()</code> y cada vez que se vuelve a
     * visualizar este applet por la página web.</p>
     */
    @Override
    public void start() {
        super.start();
        System.err.println("start");
    }

    /**
     * Es llamado por el navegador para informar al applet de que debería de
     * parar su ejecución.
     *
     * <p>Se llama cuando la página web que contiene a este applet es sustituída
     * por otra y justo antes de que este applet sea destruído.</p>
     */
    @Override
    public void stop() {
        super.stop();
        System.err.println("stop");
    }

    /**
     * Es llamado por el navegador para informar al applet de que está siendo
     * reclamado y de que debería destruir cualquier recurso que haya reservado.
     *
     * <p>Siempre se llama a <code>stop()</code> antes que a éste.</p>
     */
    @Override
    public void destroy() {
        super.destroy();
        /* Liberación de los recursos */
        this.libAutDNIe.liberarAlmacenCertificados();
        System.err.println("destroy");
    }

    /**
     * Carga parámetros del Applet.
     *
     * <p>Se llama desde <code>init()</code></p>
     */
    protected void loadAppletParameters() {
        String at = null;

        try {
            at = this.getParameter("width");
            this.width = Integer.valueOf(at).intValue();
        } catch (final Exception e) {
            this.width = 640;
        }

        try {
            at = this.getParameter("height");
            this.height = Integer.valueOf(at).intValue();
        } catch (final Exception e) {
            this.height = 380;
        }

        try {
            at = this.getParameter("maxwidth");
            this.maxWidth = Integer.valueOf(at).intValue();
        } catch (final Exception e) {
            this.maxWidth = 800;
        }

        try {
            at = this.getParameter("maxheigth");
            this.maxHeight = Integer.valueOf(at).intValue();
        } catch (final Exception e) {
            this.maxHeight = 600;
        }

    }

    /**
     * Obtener información del Applet.
     *
     * @return  la información
     */
    @Override
    public String getAppletInfo() {
        super.getAppletInfo();
        return "AppletDniE\n"
            + "\n"
            + "Clase creada por Jesús y Vanessa.\n"
            + "";
    }

    /**
     * Método encargado de obtener el reto firmado y el certificado de
     * autenticación del usuario.
     *
     * @return  matriz con la información
     */
    @Override
    public String[][] getParameterInfo() {
        final String info = (String) AccessController.doPrivileged(
                new PrivilegedAction<Object>() {
                    @Override
                    public Object run() {

                        try {
                            System.out.println("obtenerInfo");
                            /* Se retorna un array de Strings con el reto
                             * firmado y el certificado de autenticación */
                            return new String[][] {
                                    { "f", AppletDniE.this.firma },
                                    { "c", AppletDniE.this.certificado }
                                };

                        } catch (final Exception e) {
                            System.out.println(
                                "error obtenerinfo: " + e.getMessage());
                            return null;
                        }
                    }
                });
        return new String[][] {
                { "1", info }
            };
    }

    /**
     * Método encargado de procesar el PIN del usuario y cargar su certificado
     * de autenticación.
     */
    private void login() {

        if (this.libAutDNIe == null) {
            return;
        }

        this.msgError = "";

        // El campo del PIN no puede estar vacío ni el PIN puede ser inferior
        // a 4 cifras
        if ((this.pin.compareTo("") == 0) || (this.pin.length() < 4)) {
            /* Se lanza un mensaje de aviso al usuario */
            this.msgError = "El PIN no puede ser vacío o menor que 4.";
        } else {

            try {
                // Se carga el Keystore del PKCS11 con los certificados del DNIe
                this.libAutDNIe.cargarPKCS11DNIe(this.pin);
                this.libAutDNIe.cargarAliasCertificadoAutenticacionDNIe();
                this.getRFC();

            } catch (final NoSuchAlgorithmException ex) {
                this.msgError = ex.getMessage();
            } catch (final CertificateException ex) {
                this.msgError = ex.getMessage();
            } catch (final IOException ex) {
                this.msgError = ex.getMessage();
            } catch (final Exception ex) {
                this.msgError = ex.getMessage();
            }

            this.msgError.replaceAll("PKCS11 not found", "PKCS11 no hallado");
            this.msgError.replaceAll("Error parsing configuration",
                "Error analizando configuración");
        }
    }

    private void getRFC() {

        try {
            this.libAutDNIe.cargarAliasCertificadoAutenticacionDNIe();
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /* Inicialización de los objetos firma y certificado */
        try {
            System.out.println("reto " + this.reto);
            /* Realización de la firma del reto usando el certificado de
             * autenticación seleccionado por el usuario */
// this.firma = this.libAutDNIe.autenticacionDNIe(this.reto, alias);
// System.out.println("firma " + this.firma);

            /* Obtención del certificado de autenticación del usuario */
// this.certificado = new String(Base64.encode(
// this.libAutDNIe.obtenerCertificadoAutenticacionDNIe(
// alias)));
            System.out.println("cerfificado " + this.certificado);

        } catch (final Exception ex) {
            System.out.println("error: " + ex.getMessage());
        }

    }

    /**
     * Para poder refrescar applet.
     */
    @Override
    public void paint(final Graphics g) {
        super.paint(g);
        g.drawString(this.pin, 5, 15);
    }
}
