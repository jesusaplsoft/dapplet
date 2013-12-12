package com.apl.partymat.tools;

import java.io.IOException;

import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.cert.CertificateException;

import javax.swing.JOptionPane;


/**
 * @author  inteco
 */
public class AppletAutenticacionDNIeGUI
    extends javax.swing.JApplet {
    private static final long serialVersionUID = -40491314438590804L;

    private LibreriaAutenticacionDNIe libAutDNIe;
    private String reto;
    private String firma;
    private String certificado;

    // Variables declaration - do not modify
    private javax.swing.JButton aliasBoton;
    private javax.swing.JComboBox aliasCombo;
    private javax.swing.JLabel aliasEtiqueta;
    private javax.swing.JPanel aliasPanel;
    private javax.swing.JLabel okEtiqueta;
    private javax.swing.JPanel okPanel;
    private javax.swing.JButton pinBoton;
    private javax.swing.JPasswordField pinCampo;
    private javax.swing.JLabel pinEtiqueta;
    private javax.swing.JPanel pinPanel;
    // End of variables declaration

    @Override
    public void start() {
    }

    /**
     * Initializes the applet AppletAutenticacionDNIeGUI.
     */
    @Override
    public void init() {
        this.libAutDNIe = new LibreriaAutenticacionDNIe();

        /* Inicialización de los componentes gráficos de la aplicación */
        this.initComponents();
    }

    @Override
    public void stop() {
    }

    @Override
    public void destroy() {
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
                    AppletAutenticacionDNIeGUI.this.reto = datos;

                    /* Se ocultan las vistas que todavía no deben visualizarse
                     */
                    AppletAutenticacionDNIeGUI.this.aliasPanel.setVisible(
                        false);
                    AppletAutenticacionDNIeGUI.this.okPanel.setVisible(false);

                    /* Se muestra la el JApplet */
                    AppletAutenticacionDNIeGUI.this.setVisible(true);

                    return null;
                }
            });
    }

    /**
     * Método encargado de obtener el reto firmado y el certificado de
     * autenticación del usuario.
     *
     * @return  matriz de parámetros
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
                                    {
                                        "f",
                                        AppletAutenticacionDNIeGUI.this.firma
                                    },
                                    {
                                        "2",
                                        AppletAutenticacionDNIeGUI.this
                                            .certificado
                                    }
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

        final String pin = new String(this.pinCampo.getPassword());

        /* El campo del PIN no puede estar vacio ni el PIN puede ser inferior a
         * 4 cifras */
        if ((pin.compareTo("") == 0) || (pin.length() < 4)) {
            /* Se lanza un mensaje de aviso al usuario */
            JOptionPane.showMessageDialog(this.pinPanel,
                "El PIN no puede ser vacio o menor que 4.");
        } else {

            try {
                /* Se carga el Keystore del PKCS11 con los certificados del DNIe
                 */
                this.libAutDNIe.cargarPKCS11DNIe(pin);

                /* Se muestra el certificado de autenticación del usuario */
                this.aliasCombo.addItem(this.libAutDNIe
                    .cargarAliasCertificadoAutenticacionDNIe());

            } catch (final NoSuchAlgorithmException ex) {
                System.out.println("error ns: " + ex.getMessage());
            } catch (final CertificateException ex) {
                System.out.println("error cer: " + ex.getMessage());
            } catch (final IOException ex) {
                System.out.println("error io: " + ex.getMessage());
            } catch (final Exception ex) {
                System.out.println("error ex: " + ex.getMessage());
            }
        }
    }

    /**
     * This method is called from within the init() method to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        this.pinPanel = new javax.swing.JPanel();
        this.pinEtiqueta = new javax.swing.JLabel();
        this.pinCampo = new javax.swing.JPasswordField();
        this.pinBoton = new javax.swing.JButton();
        this.aliasPanel = new javax.swing.JPanel();
        this.aliasEtiqueta = new javax.swing.JLabel();
        this.aliasCombo = new javax.swing.JComboBox();
        this.aliasBoton = new javax.swing.JButton();
        this.okPanel = new javax.swing.JPanel();
        this.okEtiqueta = new javax.swing.JLabel();

        this.setVisible(false);
        this.getContentPane().setLayout(null);

        this.pinPanel.setLayout(new java.awt.GridLayout(3, 1));

        this.pinEtiqueta.setText("Introduce el PIN del DNIe:");
        this.pinPanel.add(this.pinEtiqueta);
        this.pinPanel.add(this.pinCampo);

        this.pinBoton.setText("OK");
        this.pinBoton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(
                        final java.awt.event.ActionEvent evt) {
                    AppletAutenticacionDNIeGUI.this.pinBotonActionPerformed(
                        evt);
                }
            });
        this.pinPanel.add(this.pinBoton);

        this.getContentPane().add(this.pinPanel);
        this.pinPanel.setBounds(0, 0, 400, 80);

        this.aliasPanel.setLayout(new java.awt.GridLayout(3, 1));

        this.aliasEtiqueta.setText(
            "Selecciona tu certificado para autenticarte:");
        this.aliasPanel.add(this.aliasEtiqueta);

        this.aliasCombo.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] {}));
        this.aliasPanel.add(this.aliasCombo);

        this.aliasBoton.setText("SELECCIONAR");
        this.aliasBoton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(
                        final java.awt.event.ActionEvent evt) {
                    AppletAutenticacionDNIeGUI.this.aliasBotonActionPerformed(
                        evt);
                }
            });
        this.aliasPanel.add(this.aliasBoton);

        this.getContentPane().add(this.aliasPanel);
        this.aliasPanel.setBounds(0, 0, 400, 80);

        this.okEtiqueta.setForeground(new java.awt.Color(15, 177, 14));
        this.okEtiqueta.setHorizontalAlignment(
            javax.swing.SwingConstants.CENTER);
        this.okEtiqueta.setText("Firma del reto realizada correctamente");

        final javax.swing.GroupLayout okPanelLayout =
            new javax.swing.GroupLayout(this.okPanel);
        this.okPanel.setLayout(okPanelLayout);
        okPanelLayout.setHorizontalGroup(
            okPanelLayout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                    okPanelLayout.createSequentialGroup().addGap(65, 65, 65)
                        .addComponent(this.okEtiqueta)
                        .addContainerGap(65, Short.MAX_VALUE)));
            okPanelLayout.setVerticalGroup(
                okPanelLayout.createParallelGroup(
                        javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                        okPanelLayout.createSequentialGroup()
                            .addContainerGap(32, Short.MAX_VALUE)
                            .addComponent(this.okEtiqueta).addGap(30, 30, 30)));

                this.getContentPane().add(this.okPanel);
                this.okPanel.setBounds(0, 0, 400, 80);
            } // </editor-fold>

            /**
             * Método encargado de captar el evento lanzado al presionar el
             * botón de la vista de inserción de PIN.
             *
             * @param  evt
             */
            private void pinBotonActionPerformed(
                    final java.awt.event.ActionEvent evt) {

                /* Al introducir el PIN y presionar el botón se prosigue
                 * llamando el método login() */
                this.login();

                /* Se oculta la vista para la inserción del PIN */
                this.pinPanel.setVisible(false);
                this.remove(this.pinPanel);

                /* Se muestra la nueva vista para que el usuario seleccione el
                 * certificado con el que desea autenticarse */
                this.aliasPanel.setVisible(true);
            }

            /**
             * Método encargado de captar el evento lanzado al presionar el
             * botón de la vista de selección de certificado.
             *
             * @param  evt
             */
            private void aliasBotonActionPerformed(
                    final java.awt.event.ActionEvent evt) {
                this.aliasCombo.getSelectedItem();

                /* Inicialización de los objetos firma y certificado */
                try {
                    System.out.println("reto " + this.reto);
                    /* Realización de la firma del reto usando el certificado de
                     * autenticación seleccionado por el usuario */
// this.firma = this.libAutDNIe.autenticacionDNIe(this.reto,
// alias);
// System.out.println("firma " + this.firma);

                    /* Obtención del certificado de autenticación del usuario */
// this.certificado = new String(Base64.encode(
// this.libAutDNIe
// .obtenerCertificadoAutenticacionDNIe(
// alias)));
                    System.out.println("cerfificado " + this.certificado);

                } catch (final Exception ex) {
                    System.out.println("error: " + ex.getMessage());
                }

                /* Se oculta la vista de selección de certificado */
                this.aliasPanel.setVisible(false);
                this.remove(this.aliasPanel);

                /* Se muestra la vista de confirmación de la firma del reto */
                this.okPanel.setVisible(true);

                /* Liberación de los recursos */
                this.libAutDNIe.liberarAlmacenCertificados();
            }

        }
