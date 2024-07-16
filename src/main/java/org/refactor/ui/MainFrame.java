package org.refactor.ui;

import java.awt.Component;

import org.refactor.iso8583.exceptions.Iso8583InvalidFormatException;
import org.refactor.tcp.Server;
import org.refactor.tcp.Server.ClientListener;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.swing.*;

import org.refactor.tcp.data.Client;

/**
 *
 * @author PathTrack
 */
public class MainFrame extends JFrame {

    public static final String PROGRAM_PATH = URLDecoder.decode(new File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(), StandardCharsets.UTF_8);
    private Server server;
    private JPanel dataPanel;
    private JButton serverBtn;
    private JButton settingsBtn;
    public static int delay;
    public JTextArea textDelay;

    private final ClientListener clientListener = new ClientListener() {
        @Override
        public void onNewClient(String clientIp) {
            /*FlowLayout layout = ((FlowLayout) dataPanel.getLayout());
            if (dataPanel.getComponentCount() + 1 > (layout.getHgap() * layout.getVgap())) {
                layout.setHgap(layout.getHgap() + 1);
            }
            JButton btn = new JButton(clientIp);
            btn.setName(clientIp);
            btn.setEnabled(false);
            btn.addActionListener((ActionEvent e) -> {
                Client clientTarget = server.getClient(clientIp);
                if (clientTarget == null) {
                    dataPanel.remove(searchComponentByName(dataPanel, clientIp));
                    dataPanel.validate();
                    dataPanel.repaint();
                    return;
                }
                CommunicationDialog dialog = new CommunicationDialog(MainFrame.this, true, clientTarget);
                dialog.setVisible(true);
            });
            dataPanel.add(btn);
            dataPanel.validate();
            dataPanel.repaint();*/
        }

        @Override
        public void onReceivedOfClient(String clientIp) {
            /*searchComponentByName(dataPanel, clientIp).setEnabled(true);
            dataPanel.validate();
            dataPanel.repaint();*/
        }

        @Override
        public void onLostClient(String clientIp) {
            /*dataPanel.remove(searchComponentByName(dataPanel, clientIp));
            dataPanel.validate();
            dataPanel.repaint();*/
        }
    };

    public MainFrame() {
        initUI();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (server != null) {
                    try {
                        server.close();
                    } catch (IOException ignored) {
                    }
                }
                super.windowClosing(e);
            }

        });
    }

    private void initUI() {
        initFrame();
        //root jpanel
        JPanel root = new JPanel();
        root.setLayout(null);
        add(root);
        //all components
        initComponents(root);
    }

    private void initFrame() {
        //Tamaño
        setSize(666, 444);
        setResizable(false);
        //Default
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("KotyISO8583");
        //Centrar
        setLocationRelativeTo(null);
    }

    private void initComponents(JPanel root) {
        serverBtn = new JButton("Start");
        serverBtn.setBounds(295, 30, 75, 25);
        serverBtn.setFocusable(false);
        serverBtn.addActionListener(this::serverOnClick);
        root.add(serverBtn);

        settingsBtn = new JButton(new ImageIcon(getClass().getResource("/org/refactor/images/settings_ico.png")));//unfinish
        textDelay = new JTextArea();
        textDelay.setBounds(295, 70, 75, 25);
        root.add(textDelay);
    }

    private Component searchComponentByName(JPanel panel, String judgment) {
        Component[] subComponents = panel.getComponents();
        for (Component subComponent : subComponents) {
            if (subComponent.getName().equals(judgment)) {
                return subComponent;
            }
        }
        return null;
    }

    private void serverOnClick(ActionEvent ae) {
        if (server == null) {
            try {
                delay = Integer.parseInt(textDelay.getText());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            settingsBtn.setEnabled(false);
            try {
                server = new Server(2520, clientListener);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo crear el servidor", "Server Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                server.searchClients();
            } catch (Iso8583InvalidFormatException e) {
                serverOnClick(ae);
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            serverBtn.setText("Stop");
        } else {
            try {
                server.close();
            } catch (IOException ex) {
            }
            server = null;
            serverBtn.setText("Start");
            settingsBtn.setEnabled(true);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Crear y mostrar el frame
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
