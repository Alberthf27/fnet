package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import modelo.Empleado;
import vista.PanelClientes;

public class Principal extends javax.swing.JFrame {

    private JProgressBar barraCarga; // <--- AGREGA ESTA LÍNEA

    private modelo.Empleado empleadoActual;
    public static Principal instancia;
    private Empleado empleadoLogueado;

    // CONSTRUCTOR 1: El que usa NetBeans o pruebas rápidas (Vacío)
    public Principal() {
        this.empleadoLogueado = null; // Pon null si no hay usuario
        instancia = this;
        initComponents();
        personalizarInterfaz();

        // Cargar diseño por defecto para pruebas
        configurarDiseñoModerno();
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
    }

    public Principal(modelo.Empleado empleado) {
        // --- 1. ESTA ES LA LÍNEA QUE FALTABA ---
        instancia = this; // <--- SIN ESTO, LOS PANELES NO PUEDEN COMUNICARSE
        // ---------------------------------------

        // 1. Configuración visual
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Error iniciando FlatLaf");
        }

        // 2. Iniciar componentes
        initComponents();

        this.empleadoLogueado = empleado;
        this.setLocationRelativeTo(null);

        // 3. Configurar interfaz
        personalizarInterfaz();

        // 4. Iniciar Dashboard
        btn_principal.doClick();
        configurarDiseñoModerno();
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        btn_clientes = new javax.swing.JButton();
        btn_principal = new javax.swing.JButton();
        btn_servicios = new javax.swing.JButton();
        btn_pagos = new javax.swing.JButton();
        btn_instalaciones = new javax.swing.JButton();
        btn_equipos = new javax.swing.JButton();
        btn_usuarios = new javax.swing.JButton();
        btn_salir = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        lbl_usuario = new javax.swing.JLabel();
        btn_finanzas = new javax.swing.JButton();
        content = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setExtendedState(6);

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));

        jPanel1.setBackground(new java.awt.Color(0, 102, 204));

        btn_clientes.setText("Clientes");
        btn_clientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_clientesActionPerformed(evt);
            }
        });

        btn_principal.setText("Dshboard");
        btn_principal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_principalActionPerformed(evt);
            }
        });

        btn_servicios.setText("Servicios");
        btn_servicios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_serviciosActionPerformed(evt);
            }
        });

        btn_pagos.setText("Pagos");
        btn_pagos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_pagosActionPerformed(evt);
            }
        });

        btn_instalaciones.setText("Instalaciones");
        btn_instalaciones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_instalacionesActionPerformed(evt);
            }
        });

        btn_equipos.setText("Equipos");
        btn_equipos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_equiposActionPerformed(evt);
            }
        });

        btn_usuarios.setText("Usuarios");
        btn_usuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_usuariosActionPerformed(evt);
            }
        });

        btn_salir.setText("Salir");
        btn_salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_salirActionPerformed(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Sin título (1)_1.png"))); // NOI18N

        lbl_usuario.setForeground(new java.awt.Color(204, 204, 204));
        lbl_usuario.setText("Usuario: Usuario1");

        btn_finanzas.setText("Finanzas");
        btn_finanzas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_finanzasActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(jPanel1Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btn_principal, javax.swing.GroupLayout.DEFAULT_SIZE, 154,
                                                Short.MAX_VALUE)
                                        .addComponent(btn_clientes, javax.swing.GroupLayout.DEFAULT_SIZE, 154,
                                                Short.MAX_VALUE)
                                        .addComponent(btn_pagos, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btn_servicios, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btn_instalaciones, javax.swing.GroupLayout.DEFAULT_SIZE, 154,
                                                Short.MAX_VALUE)
                                        .addComponent(btn_equipos, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btn_usuarios, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btn_salir, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lbl_usuario, javax.swing.GroupLayout.Alignment.TRAILING,
                                                javax.swing.GroupLayout.PREFERRED_SIZE, 145,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btn_finanzas, javax.swing.GroupLayout.DEFAULT_SIZE, 154,
                                                Short.MAX_VALUE))
                                .addContainerGap(20, Short.MAX_VALUE)));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 90,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_principal, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(11, 11, 11)
                                .addComponent(btn_finanzas, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btn_clientes, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btn_pagos, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btn_servicios, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btn_instalaciones, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btn_equipos, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btn_usuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btn_salir, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 204,
                                        Short.MAX_VALUE)
                                .addComponent(lbl_usuario, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)));

        content.setBackground(new java.awt.Color(255, 255, 255));
        content.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(content, javax.swing.GroupLayout.DEFAULT_SIZE, 1214, Short.MAX_VALUE)));
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(content, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void personalizarInterfaz() {
        // 1. CREAR LA BARRA MANUALMENTE
        barraCarga = new JProgressBar();
        barraCarga.setIndeterminate(true);
        barraCarga.setStringPainted(false);

        // Estilo visual
        barraCarga.setBorderPainted(false);
        barraCarga.setOpaque(false);
        barraCarga.setForeground(new java.awt.Color(255, 165, 0)); // Naranja
        barraCarga.setBackground(new java.awt.Color(0, 102, 204)); // Azul del menú
        barraCarga.setVisible(false);

        // 2. AGREGARLA A LA CAPA FLOTANTE (LAYERED PANE)
        // Esto permite que esté "encima" sin empujar nada
        getLayeredPane().add(barraCarga, javax.swing.JLayeredPane.PALETTE_LAYER);

        // 3. POSICIONAMIENTO DINÁMICO (El Truco)
        // Esto asegura que la barra siempre esté exactamente sobre el jPanel1
        // aunque muevas o redimensiones la ventana.
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                posicionarBarra();
            }
        });

        // Posicionar por primera vez
        posicionarBarra();

        // 4. DATOS DEL USUARIO (Tu código normal)
        if (empleadoLogueado != null) {
            String nombre = empleadoLogueado.getNombres();
            String rol = empleadoLogueado.getCargo();
            if (rol == null || rol.isEmpty()) {
                rol = "Sin Cargo";
            }
            lbl_usuario.setText("<html><div style='text-align: right; color: #CCCCCC;'><b>" + nombre
                    + "</b><br><span style='font-size:10px;'>" + rol + "</span></div></html>");
            this.setTitle("ISP Manager - " + nombre);
        }
    }

    // Método auxiliar para mantener la barra pegada al menú azul
    private void posicionarBarra() {
        if (jPanel1 != null && barraCarga != null && jPanel1.isShowing()) {
            // 1. TRUCO MATEMÁTICO:
            // Preguntamos: "¿En qué coordenada X,Y de la pantalla está el jPanel1?"
            // y convertimos esa posición al sistema de coordenadas de la capa flotante.
            java.awt.Point p = javax.swing.SwingUtilities.convertPoint(
                    jPanel1.getParent(),
                    jPanel1.getLocation(),
                    getLayeredPane());

            // 2. Aplicamos esa posición exacta a la barra
            // p.x y p.y son el punto exacto donde empieza la esquina del panel azul
            barraCarga.setBounds(p.x, p.y, jPanel1.getWidth(), 6);

            // Aseguramos que se redibuje
            barraCarga.revalidate();
            barraCarga.repaint();
        }
    }

    private void btn_clientesActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_clientesActionPerformed
        marcarBotonActivo(btn_clientes);
        // Carga el panel con pestañas de clientes
        MostrarPanel(new vista.PanelClientes());
    }// GEN-LAST:event_btn_clientesActionPerformed

    private void btn_principalActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_principalActionPerformed
        marcarBotonActivo(btn_principal);

        // AQUÍ ESTÁ EL CAMBIO: Llamamos a panel_Gerente
        MostrarPanel(new vista.panel_Gerente());
    }// GEN-LAST:event_btn_principalActionPerformed

    private void btn_pagosActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_pagosActionPerformed

        marcarBotonActivo(btn_pagos);
        MostrarPanel(new vista.PanelPagos()); // <--- Llamada al nuevo panel

    }// GEN-LAST:event_btn_pagosActionPerformed

    private void btn_serviciosActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_serviciosActionPerformed
        marcarBotonActivo(btn_servicios);
        MostrarPanel(new vista.panel_Servicios());
    }// GEN-LAST:event_btn_serviciosActionPerformed

    private void btn_instalacionesActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_instalacionesActionPerformed
        marcarBotonActivo(btn_instalaciones);
        MostrarPanel(new vista.PanelInstalaciones());
    }// GEN-LAST:event_btn_instalacionesActionPerformed

    private void btn_equiposActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_equiposActionPerformed
        marcarBotonActivo(btn_equipos);
        MostrarPanel(new vista.PanelEquipos());
    }// GEN-LAST:event_btn_equiposActionPerformed

    private void btn_usuariosActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_usuariosActionPerformed
        marcarBotonActivo(btn_usuarios);
        MostrarPanel(new vista.PanelUsuarios());
    }// GEN-LAST:event_btn_usuariosActionPerformed

    private void btn_salirActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_salirActionPerformed
        int confirmacion = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que deseas cerrar sesión?",
                "Cerrar Sesión",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);

        if (confirmacion == javax.swing.JOptionPane.YES_OPTION) {
            // 2. Cerrar la ventana actual (Principal)
            this.dispose();

            // 3. Abrir la ventana de Login
            // Asegúrate de que "vista.Login" sea la ruta correcta a tu clase Login
            new vista.Login().setVisible(true);
        }
    }// GEN-LAST:event_btn_salirActionPerformed

    private void btn_finanzasActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btn_finanzasActionPerformed
        marcarBotonActivo(btn_finanzas);
        MostrarPanel(new vista.PanelFinanzas());

    }// GEN-LAST:event_btn_finanzasActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            // Poner esto ANTES de iniciar la ventana
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Fallo al iniciar FlatLaf");
        }

        java.awt.EventQueue.invokeLater(() -> {
            new Principal(null).setVisible(true); // O tu constructor correspondiente
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_clientes;
    private javax.swing.JButton btn_equipos;
    private javax.swing.JButton btn_finanzas;
    private javax.swing.JButton btn_instalaciones;
    private javax.swing.JButton btn_pagos;
    private javax.swing.JButton btn_principal;
    private javax.swing.JButton btn_salir;
    private javax.swing.JButton btn_servicios;
    private javax.swing.JButton btn_usuarios;
    private javax.swing.JPanel content;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lbl_usuario;

    // End of variables declaration//GEN-END:variables
    private void configurarDiseñoModerno() {
        // 1. Colores (Iguales que antes)
        java.awt.Color colorSidebar = new java.awt.Color(15, 23, 42);
        java.awt.Color colorBgRight = new java.awt.Color(241, 245, 249);

        jPanel1.setBackground(colorSidebar);
        jPanel2.setBackground(colorBgRight);

        // 2. Configurar botones CON TUS IMÁGENES
        // Asegúrate de que los nombres aquí coincidan con los archivos que pegaste en
        // "img"
        configurarBoton(btn_principal, "Dashboard", "/img/dashboard.png");
        configurarBoton(btn_clientes, "Clientes", "/img/clientes.png");
        configurarBoton(btn_pagos, "Pagos", "/img/pagos.png");
        configurarBoton(btn_servicios, "Servicios", "/img/servicios.png");
        configurarBoton(btn_instalaciones, "Instalaciones", "/img/instalaciones.png");
        configurarBoton(btn_equipos, "Equipos", "/img/equipos.png");
        configurarBoton(btn_usuarios, "Usuarios", "/img/usuarios.png");
        configurarBoton(btn_salir, "Salir", "/img/salir.png");
        configurarBoton(btn_finanzas, "Finanzas", "/img/finanzas.png");

        // 3. Activar el primero
        marcarBotonActivo(btn_principal);
    }

    private void configurarBoton(javax.swing.JButton btn, String texto, String rutaIcono) {
        // 1. Cargar el Icono desde la carpeta
        try {
            // getClass().getResource busca dentro de los paquetes del proyecto
            javax.swing.ImageIcon icono = new javax.swing.ImageIcon(getClass().getResource(rutaIcono));
            btn.setIcon(icono);
        } catch (Exception e) {
            System.err.println("Error cargando icono: " + rutaIcono);
        }

        // 2. Texto y Espaciado
        btn.setText("   " + texto); // Espacio entre icono y texto
        btn.setIconTextGap(5); // Separación extra

        // 3. Estilo Visual (Igual que antes)
        btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        btn.setForeground(new java.awt.Color(148, 163, 184)); // Gris inactivo

        // Limpieza de bordes
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setBackground(new java.awt.Color(15, 23, 42)); // Fondo oscuro

        btn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btn.setMargin(new java.awt.Insets(10, 20, 10, 10));

        // 4. Efecto Hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.getBackground().getBlue() == 42) {
                    btn.setBackground(new java.awt.Color(30, 41, 59));
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn.getBackground().getBlue() == 59) {
                    btn.setBackground(new java.awt.Color(15, 23, 42));
                }
            }
        });
    }

    /**
     * Transforma un botón normal en uno estilo Dashboard moderno.
     */
    private void estilarBotonMenu(javax.swing.JButton btn) {
        btn.setForeground(new Color(148, 163, 184)); // Texto gris claro (inactivo)
        btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));

        // Quitar bordes y fondos por defecto de Java
        btn.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 20, 12, 10)); // Espaciado interno
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); // Transparente por defecto
        btn.setOpaque(true);
        btn.setBackground(new Color(15, 23, 42)); // Fondo igual al menú (invisible)
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Alineación a la izquierda para que parezca menú
        btn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        // Efecto Hover (pasar el mouse)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Solo cambia de color si NO está activo actualmente
                if (btn.getForeground().equals(new Color(148, 163, 184))) {
                    btn.setBackground(new Color(30, 41, 59)); // Un poco más claro al pasar mouse
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Si no es el activo, vuelve a transparente
                if (btn.getForeground().equals(new Color(148, 163, 184))) {
                    btn.setBackground(new Color(15, 23, 42));
                }
            }
        });
    }

    /**
     * Método MÁGICO: Pinta el botón clickeado de azul brillante y apaga los
     * demás.
     */
    private void marcarBotonActivo(javax.swing.JButton btnActivo) {
        // Lista de todos tus botones
        javax.swing.JButton[] todos = {
                btn_principal, btn_clientes, btn_pagos, btn_servicios,
                btn_instalaciones, btn_equipos, btn_usuarios, btn_salir,
                btn_finanzas // <--- ¡AQUÍ FALTABA ESTE!
        };

        java.awt.Color colorActivo = new java.awt.Color(37, 99, 235); // AZUL BRILLANTE (Blue 600)
        java.awt.Color colorInactivo = new java.awt.Color(15, 23, 42); // AZUL OSCURO

        for (javax.swing.JButton b : todos) {
            if (b == btnActivo) {
                b.setBackground(colorActivo); // Fondo Azul Brillante
                b.setForeground(java.awt.Color.WHITE); // Texto Blanco
            } else {
                b.setBackground(colorInactivo); // Fondo Oscuro
                b.setForeground(new java.awt.Color(148, 163, 184)); // Texto Gris
            }
        }
    }

    public void mostrarCarga(boolean estado) {
        SwingUtilities.invokeLater(() -> {
            if (barraCarga != null) {
                barraCarga.setVisible(estado);
                // Forzamos que se redibuje encima de todo
                if (estado) {
                    barraCarga.repaint();
                }
            }
        });
    }

    /**
     * Obtener el empleado que está actualmente logueado
     */
    public modelo.Empleado getEmpleadoLogueado() {
        return this.empleadoLogueado;
    }

    /**
     * Método para cambiar paneles
     */
    private void MostrarPanel(javax.swing.JPanel p) {
        // Configuración básica del panel hijo
        p.setSize(1080, 720);
        p.setLocation(0, 0);

        // Limpiar contenido anterior
        content.removeAll();

        // --- SOLUCIÓN DEL ERROR ---
        // Forzamos el Layout a BorderLayout para que acepte el comando .CENTER
        content.setLayout(new java.awt.BorderLayout());
        // --------------------------

        content.add(p, java.awt.BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

}
