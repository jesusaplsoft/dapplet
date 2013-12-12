package com.apl.partymat.tools;

import net.miginfocom.swing.MigLayout;

import org.bouncycastle.util.encoders.Base64;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import java.net.MalformedURLException;

import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.cert.CertificateException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;


/**
 * Obtener dni electrónico conectado a una SmartCard.
 *
 * <p>Para ello, pide el PIN y accede al dni electrónico que se conecte a la
 * SmartCard.</p>
 *
 * @author  jesus
 */
public class GetDniE
    extends JApplet {

    private static final long serialVersionUID = 7217692140248075908L;
    private JPasswordField passwordField;
    private JLabel lblError;
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
    public GetDniE()
            throws HeadlessException {
    }

    /**
     * Inicializa componentes gráficos.
     *
     * <p>Se llama desde <code>init()</code></p>
     */
    public void initComponents() {
        final Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout(0, 0));
        this.setSize(this.width, this.height);

        final JPanel panelNorte = new JPanel();
        this.getContentPane().add(panelNorte, BorderLayout.NORTH);
        panelNorte.setLayout(new BoxLayout(panelNorte, BoxLayout.X_AXIS));

        final JLabel lblLogo = new JLabel("");
        lblLogo.setDoubleBuffered(true);
        lblLogo.setPreferredSize(new Dimension(200, 200));
        lblLogo.setHorizontalAlignment(SwingConstants.LEFT);
        lblLogo.setVerticalAlignment(SwingConstants.TOP);

        try {
            lblLogo.setIcon(this.getImageIcon("/resources/logo.jpg",
                    lblLogo.getPreferredSize().width,
                    lblLogo.getPreferredSize().height));
        } catch (final Exception e) {
            System.err.println("Error: " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        panelNorte.add(lblLogo);

        final JLabel lblTitle = new JLabel(
                "<html>Autenticaci\u00F3n de usuario<br/> "
                + "mediante Dni Electr\u00F3nico");
        lblTitle.setDoubleBuffered(true);
        lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 34));
        lblTitle.setHorizontalAlignment(SwingConstants.RIGHT);
        panelNorte.add(lblTitle);

        final JLabel lblFiller = new JLabel("    ");
        lblFiller.setDoubleBuffered(true);
        panelNorte.add(lblFiller);

        final JPanel panelCentro = new JPanel();
        this.getContentPane().add(panelCentro, BorderLayout.CENTER);
        panelCentro.setLayout(new MigLayout("",
                "[57px][246px][46px][][][][][][][][]", "[20px][][]"));

        final JLabel lblPassword = new JLabel("PIN del dniE");
        lblPassword.setHorizontalAlignment(SwingConstants.RIGHT);
        panelCentro.add(lblPassword, "cell 6 0,alignx left,aligny center");
        lblPassword.setLabelFor(this.passwordField);

        this.passwordField = new JPasswordField();
        this.passwordField.setHorizontalAlignment(SwingConstants.LEFT);
        this.passwordField.setColumns(30);
        panelCentro.add(this.passwordField, "cell 8 0,alignx left,aligny top");

        this.lblError = new JLabel("");
        this.lblError.setForeground(Color.RED);
        panelCentro.add(this.lblError, "cell 8 2 2 1");

        final JPanel panelSur = new JPanel();
        panelSur.getLayout();
        this.getContentPane().add(panelSur, BorderLayout.SOUTH);

        final JButton btnOk = new JButton("Ok");
        btnOk.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    GetDniE.this.login();

// if (GetDniE.this.lblError.getText().isEmpty())
// contentPane.setVisible(false);
                }
            });
        btnOk.setDoubleBuffered(true);
        btnOk.setMnemonic('k');
        panelSur.add(btnOk);
        this.passwordField.requestFocus();
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

        this.libAutDNIe = new LibreriaAutenticacionDNIe();
        this.loadAppletParameters();

        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        GetDniE.this.initComponents();
                    }
                });
        } catch (final Exception e) {
            System.err.println(
                "initComponents no se ha completado correctamente");
        }
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
    }

    /**
     * Es llamado por el navegador para informar al applet de que está siendo
     * reclamado y de que debería destruir cualquier recurso que haya reservado.
     *
     * <p>Siempre se llama a <code>stop()</code> antes que a éste.</p>
     */
    @Override
    public void destroy() {
    }

    /**
     * Lee texto pasado y lo devuelve como imagen de un tamaño determinado.
     *
     * @param   imagen  a procesar
     * @param   width   ancho nuevo
     * @param   height  alto nuevo
     *
     * @return  imagen procesada
     *
     * @throws  MalformedURLException  por error
     */
    protected ImageIcon getImageIcon(final String imagen,
            final int width,
            final int height)
            throws MalformedURLException {
        // carga la imagen en un ImageIcon
        final ImageIcon imageIcon = new ImageIcon(GetDniE.class.getResource(
                    imagen));
        // la transforma
        final Image image = imageIcon.getImage();
        // la escala suavemente
        final Image newimg = image.getScaledInstance(width, height,
                java.awt.Image.SCALE_SMOOTH);
        // transforma de vuelta
        return new ImageIcon(newimg);
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
     * Método encargado de empezar el proceso de autenticación pasando el reto
     * del servidor.
     */
    public void lanzarInterfaz() {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    GetDniE.this.setVisible(true);
                    return null;
                }
            });
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
                                    { "f", GetDniE.this.firma },
                                    { "c", GetDniE.this.certificado }
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

        this.lblError.setText("");

        final String pin = new String(this.passwordField.getPassword());

        // El campo del PIN no puede estar vacío ni el PIN puede ser inferior
        // a 4 cifras
        if ((pin.compareTo("") == 0) || (pin.length() < 4)) {
            /* Se lanza un mensaje de aviso al usuario */
            this.lblError.setText("El PIN no puede ser vacio o menor que 4.");
            this.passwordField.requestFocus();
        } else {

            try {
                // Se carga el Keystore del PKCS11 con los certificados del DNIe
                this.libAutDNIe.cargarPKCS11DNIe(pin);
                this.libAutDNIe.cargarAliasCertificadoAutenticacionDNIe();
                this.getRFC();

            } catch (final NoSuchAlgorithmException ex) {
// System.out.println("error ns: " + ex.getMessage());
                this.lblError.setText(ex.getMessage());
                this.passwordField.requestFocus();
            } catch (final CertificateException ex) {
// System.out.println("error cer: " + ex.getMessage());
                this.lblError.setText(ex.getMessage());
                this.passwordField.requestFocus();
            } catch (final IOException ex) {
// System.out.println("error io: " + ex.getMessage());
                this.lblError.setText(ex.getMessage());
                this.passwordField.requestFocus();
            } catch (final Exception ex) {
// System.out.println("error ex: " + ex.getMessage());
                this.lblError.setText(ex.getMessage());
                this.passwordField.requestFocus();
            }

            this.lblError.setText(
                this.lblError.getText()
                    .replaceAll("PKCS11 not found", "PKCS11 no hallado"));
            this.lblError.setText(
                this.lblError.getText()
                    .replaceAll("Error parsing configuration",
                        "Error analizando configuración"));
        }
    }

    private void getRFC() {
        String alias = null;

        try {
            alias = this.libAutDNIe.cargarAliasCertificadoAutenticacionDNIe();
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /* Inicialización de los objetos firma y certificado */
        try {
            System.out.println("reto " + this.reto);
            /* Realización de la firma del reto usando el certificado de
             * autenticación seleccionado por el usuario */
            this.firma = this.libAutDNIe.autenticacionDNIe(this.reto, alias);
            System.out.println("firma " + this.firma);

            /* Obtención del certificado de autenticación del usuario */
            this.certificado = new String(Base64.encode(
                        this.libAutDNIe.obtenerCertificadoAutenticacionDNIe(
                            alias)));
            System.out.println("cerfificado " + this.certificado);

        } catch (final Exception ex) {
            System.out.println("error: " + ex.getMessage());
        }

        /* Liberación de los recursos */
        this.libAutDNIe.liberarAlmacenCertificados();
    }

    /**
     * Obtener clave de usuario.
     *
     * @return  la clave validada
     */
    public String getKey() {
        return "03797140R";
    }
}
