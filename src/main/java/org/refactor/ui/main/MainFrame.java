package org.refactor.ui.main;

import org.refactor.iso8583.exceptions.Iso8583InvalidFormatException;
import org.refactor.tcp.Server;
import org.refactor.tcp.Server.ClientListener;
import org.refactor.tcp.data.Client;
import org.refactor.ui.CommunicationDialog;
import org.refactor.ui.layout_managers.percent_layout.PercentData;
import org.refactor.ui.layout_managers.percent_layout.PercentLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @author PathTrack
 */
public class MainFrame extends JFrame {

    public static final String PROGRAM_PATH = URLDecoder.decode(new File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(), StandardCharsets.UTF_8);
    private static final Dimension FRAME_REFERENCE_DIMENSION = new Dimension(960, 540);
    private Server server;
    private JPanel dataPanel;
    private JButton serverBtn;
    private JButton settingsBtn;
    private JPanel rootPanel;

    private final ClientListener clientListener = new ClientListener() {
        @Override
        public void onNewClient(String clientIp) {
            FlowLayout layout = ((FlowLayout) dataPanel.getLayout());
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
            dataPanel.repaint();
        }

        @Override
        public void onReceivedOfClient(String clientIp) {
            searchComponentByName(dataPanel, clientIp).setEnabled(true);
            dataPanel.validate();
            dataPanel.repaint();
        }

        @Override
        public void onLostClient(String clientIp) {
            dataPanel.remove(searchComponentByName(dataPanel, clientIp));
            dataPanel.validate();
            dataPanel.repaint();
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

    /**
     * Init UI components.
     */
    private void initUI() {
        initFrame();
        setRootPanel();
        addComponents(rootPanel);
    }

    /**
     * Init the main frame settings, including size, layout, and basic properties.
     */
    private void initFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("KotyISO8583");
        setLayout(new PercentLayout(FRAME_REFERENCE_DIMENSION));

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width / 2, screen.height / 2);
        setResizable(false);

        setLocationRelativeTo(null);
    }

    /**
     * Create a root configuration panel.
     *
     * @return configured {@link JPanel}
     */
    private void setRootPanel() {
        rootPanel = new JPanel(new PercentLayout(FRAME_REFERENCE_DIMENSION));
        rootPanel.setBounds(0,0,getWidth(),getHeight());
        PercentData.Builder builder = new PercentData.Builder();
        builder.setPercentBounds(new Rectangle(50, 50, 100, 100));
        add(rootPanel, builder);
    }

    /**
     * Initialize the other UI components.
     *
     * @param root {@link JPanel} to add the components
     */
    private void addComponents(JPanel root) {
        addServerBtn(root);
        addSettingsBtn(root);
    }

    private void addServerBtn(JPanel root) {
        serverBtn = new JButton("Start");
        serverBtn.addActionListener(this::serverOnClick);

        PercentData.Builder percentBuilder = new PercentData.Builder();
        percentBuilder.setPercentBounds(new Rectangle(50, 12, 7, 5));
        percentBuilder.setFont(serverBtn.getFont());

        root.add(serverBtn, percentBuilder);
    }

    private void addSettingsBtn(JPanel root) {
        settingsBtn = new JButton(new ImageIcon(getClass().getResource("/org/refactor/images/settings_ico.png")));

        PercentData.Builder percentBuilder = new PercentData.Builder();
        percentBuilder.setPercentBounds(new Rectangle(90, 10, 5, 8));
        percentBuilder.setFont(settingsBtn.getFont());

        root.add(settingsBtn, percentBuilder);
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
            settingsBtn.setEnabled(false);
            try {
                server = new Server(2020, clientListener);
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
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
            mainFrame.requestFocusInWindow();
        });
    }
}
