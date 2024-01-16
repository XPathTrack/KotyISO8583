package tcp.data;

import java.net.Socket;

/**
 *
 * @author PathTrack
 */
public class Client {
    
    private final Socket socket;

    public Client(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }
}
