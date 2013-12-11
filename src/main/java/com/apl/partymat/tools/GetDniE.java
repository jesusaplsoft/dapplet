package com.apl.partymat.tools;

import net.miginfocom.swing.MigLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Image;

import java.security.AccessController;
import java.security.PrivilegedAction;

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
    // Parámetros pasados
    String dir; // el directorio relativo a codebase
    int height; // altura del panel de contenido del applet
    int width; // anchura del panel de contenido del applet

    private LibreriaAutenticacionDNIe libAutDNIe;
    private String firma;
    private String certificado;

    private javax.swing.JPanel aliasPanel;
    private javax.swing.JPanel okPanel;

    /**
     * Constructor del formulario.
     *
     * @throws  HeadlessException  si no hay ratón, teclado o pantalla
     */
    public GetDniE()
            throws HeadlessException {
        this.initComponents();
    }

    /**
     * Inicializa componentes gráficos.
     */
    public void initComponents() {
        this.getContentPane().setLayout(new BorderLayout(0, 0));
        this.setSize(width, height);

        final JPanel panelNorte = new JPanel();
        this.getContentPane().add(panelNorte, BorderLayout.NORTH);
        panelNorte.setLayout(new BoxLayout(panelNorte, BoxLayout.X_AXIS));

        final JLabel lblLogo = new JLabel("");
        lblLogo.setDoubleBuffered(true);
        lblLogo.setPreferredSize(new Dimension(200, 200));
        lblLogo.setHorizontalAlignment(SwingConstants.LEFT);
        lblLogo.setVerticalAlignment(SwingConstants.TOP);
        lblLogo.setIcon(this.getImageIcon("/com/apl/partymat/tools/logo.jpg",
                lblLogo.getPreferredSize().width,
                lblLogo.getPreferredSize().height));
        panelNorte.add(lblLogo);

        final JLabel lblTitle = new JLabel(
                "<html>Autenticación de usuario<br/> mediante Dni Electrónico");
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

        final JLabel lblError = new JLabel("");
        panelCentro.add(lblError, "cell 8 2 2 1");

        final JPanel panelSur = new JPanel();
        panelSur.getLayout();
        this.getContentPane().add(panelSur, BorderLayout.SOUTH);

        final JButton btnOk = new JButton("Ok");
        btnOk.setDoubleBuffered(true);
        btnOk.setMnemonic('k');
        panelSur.add(btnOk);
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
     */
    protected ImageIcon getImageIcon(final String imagen,
            final int width,
            final int height) {
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
     */
    protected void loadAppletParameters() {
        // Get the applet parameters.
        String at = this.getParameter("dir");
        at = this.getParameter("width");
        this.width = (at != null) ? Integer.valueOf(at).intValue() : 0;
        at = this.getParameter("height");
        this.height = (at != null) ? Integer.valueOf(at).intValue() : 0;

    }

    /**
     * Método encargado de empezar el proceso de autenticación pasando el reto
     * del servidor.
     *
     * @param  datos  a procesar
     */
    public void lanzarInterfaz(final String datos) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    /* Se ocultan las vistas que todavía no deben visualizarse
                     */
                    GetDniE.this.aliasPanel.setVisible(false);
                    GetDniE.this.okPanel.setVisible(false);

                    /* Se muestra la el JApplet */
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
}
