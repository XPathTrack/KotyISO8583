package tcp.data;

import iso8583.data_class.IsoData;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PathTrack
 */
public class Client {
    
    private final Socket socket;
    private final List<IsoData> talkLog = new ArrayList<>();

    public Client(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public List<IsoData> getTalkLog() {
        return talkLog;
    }
}