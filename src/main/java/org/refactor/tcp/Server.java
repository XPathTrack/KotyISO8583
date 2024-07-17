package org.refactor.tcp;

import org.refactor.iso8583.exceptions.Iso8583InvalidFormatException;
import org.refactor.tcp.data.Client;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;
import org.refactor.utils.TaskThread;

/**
 *
 * @author PathTrack
 */
public class Server implements Closeable {

    private final ServerSocket server;
    private final HashMap<String, Client> clients = new HashMap<>();
    private final ClientListener clientListener;
    private boolean run = true;
    private Thread connectClients;

    public Server(int port, ClientListener clientListener) throws IOException {
        server = new ServerSocket(port);
        this.clientListener = clientListener;
    }

    public void searchClients() throws Iso8583InvalidFormatException {
        if (run && connectClients != null) {
            return;
        }
        Communication.getInstance().prepare();
        startClientManagersThreads();
    }

    private void startClientManagersThreads() {
        connectClients = new Thread(() -> {
            while (run) {
                try {
                    Socket newClient = server.accept();
                    String clientIp = newClient.getLocalAddress().getHostAddress();
                    try {
                        if (clients.containsKey(clientIp)) {
                            clientListener.onLostClient(clientIp);
                        }
                        clientListener.onNewClient(clientIp);
                    } catch (Exception ignored) {
                        try {
                            newClient.close();
                        } catch (IOException ignored2) {
                        }
                        continue;
                    }
                    clients.put(clientIp, new Client(newClient));
                    Communication.getInstance().receive(clients.get(clientIp), (boolean success) -> {
                        try {
                            if (success) {
                                clientListener.onReceivedOfClient(clientIp);
                                return;
                            }
                        } catch (Exception e) {
                        }
                        try {
                            clients.get(clientIp).getSocket().close();
                        } catch (IOException ignored) {
                        }
                        try {
                            clientListener.onLostClient(clientIp);
                        } catch (Exception ignored) {
                        }
                    });
                } catch (IOException ex) {
                }
            }
        });
        connectClients.start();
    }

    public Client getClient(String ipClient) {
        return clients.get(ipClient);
    }

    @Override
    public void close() throws IOException {
        run = false;
        Communication.getInstance().release();
        clients.clear();
        server.close();
        if (connectClients != null)
            connectClients.interrupt();
    }

    public interface ClientListener {

        void onNewClient(String clientIp);

        void onReceivedOfClient(String clientIp);

        void onLostClient(String clientIp);
    }
}