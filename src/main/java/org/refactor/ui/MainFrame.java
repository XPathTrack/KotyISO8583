package org.refactor.ui;

import java.awt.*;

import org.refactor.iso8583.exceptions.Iso8583InvalidFormatException;
import org.refactor.tcp.Server;
import org.refactor.tcp.Server.ClientListener;

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
 * @author PathTrack
 */
public class MainFrame extends JFrame {

    public static final String PROGRAM_PATH = URLDecoder.decode(new File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(), StandardCharsets.UTF_8);
    private Server server;
    private JPanel dataPanel;
    private JButton serverBtn;
    private JButton settingsBtn;

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
        JPanel root = buildRootPanel();
        add(root);
        initComponents(root);
    }

    /**
     * Init the main frame settings, including size, layout, and basic properties.
     */
    private void initFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("KotyISO8583");
        setLayout(null);

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
    private JPanel buildRootPanel() {
        JPanel root = new JPanel(null);
        root.setBounds(0, 0, getWidth(), getHeight());
        return root;
    }

    /**
     * Initialize the other UI components.
     *
     * @param root {@link JPanel} to add the components
     */
    private void initComponents(JPanel root) {
        serverBtn = new JButton("Start");
        setBounds(serverBtn, 7, 5, 50, 12);
        serverBtn.addActionListener(this::serverOnClick);
        root.add(serverBtn);

        settingsBtn = new JButton(new ImageIcon(getClass().getResource("/org/refactor/images/settings_ico.png")));
        setBounds(settingsBtn, 5, 8, 90, 10);
        root.add(settingsBtn);
    }

    /**
     * Configures the bounds of a given component within the frame using percentage-based dimensions and positioning.
     * The component's width, height, and position are calculated as percentages of the frame's dimensions.
     * The component is centered at the specified X and Y percentage positions.
     *
     * @param component     the component to configure
     * @param widthPercent  the width of the component calculated as a percentage of the frame's width (0-100)
     * @param heightPercent the height of the component calculated as a percentage of the frame's height (0-100)
     * @param xPercent      the horizontal position of the component's center calculated as a percentage of the frame's width (0-100)
     * @param yPercent      the vertical position of the component's center calculated as a percentage of the frame's height (0-100)
     */
    private void setBounds(Component component, int widthPercent, int heightPercent, int xPercent, int yPercent) {
        int width = percent(getWidth(), widthPercent);
        int height = percent(getHeight(), heightPercent);
        int x = percent(getWidth(), xPercent);
        int y = percent(getHeight(), yPercent);
        component.setBounds(x - width / 2, y - height / 2, width, height);
    }

    /**
     * Efficiently calculates a percentage of the given value.
     * Handles any percentage range with foolproof accuracy.
     *
     * @param value   the original value to calculate from
     * @param percent the percentage to apply
     * @return the calculated percentage of the original value
     */
    private int percent(int value, int percent) {
        if (percent < 1) {
            return 0;
        } else if (percent > 99) {
            return value;
        } else if (percent == 1) {
            return value / 100;
        } else {
            return (value * percent) / 100;
        }
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
