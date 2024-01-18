package tcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import iso8583.data_class.IsoData;
import iso8583.formatters.IsoFormatter;
import tcp.data.Client;
import utils.TaskThread;
import utils.ToolBox;

import javax.swing.*;

/**
 * @author PathTrack
 */
public class Communication {

    private static final Communication Instance = new Communication();

    private IsoFormatter isoFormatter;
    private final TaskThread taskExecutor = new TaskThread();

    private Communication() {
    }

    public static Communication getInstance() {
        return Instance;
    }

    public void prepareFormatter() {
        try {
            isoFormatter = new IsoFormatter();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void receive(Client client, CommunicationListener communicationListener) {
        taskExecutor.addTask(() -> {
            byte[] data;
            try {
                data = receive(client.getSocket());
            } catch (IOException ex) {
                communicationListener.onFinish(false);
                return;
            }
            IsoData isoData = isoFormatter.decode(data);

            communicationListener.onFinish(true);
        });
    }

    private byte[] receive(Socket sc) throws IOException {
        InputStream input = sc.getInputStream();
        byte[] grossLength = input.readNBytes(2); // read data length
        int lengthData = ToolBox.bytesToIntDec(0, grossLength.length, grossLength); // unpack data length
        return input.readNBytes(lengthData); // read the length obtained
    }

    public interface CommunicationListener {

        void onFinish(boolean success);
    }
}