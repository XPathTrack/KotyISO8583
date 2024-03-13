package tcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import iso8583.data_class.IsoData;
import iso8583.exceptions.Iso8583InvalidFormatException;
import iso8583.formatters.HexFormatter;
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

    public void prepare() {
        try {
            isoFormatter = new IsoFormatter();
        } catch (Iso8583InvalidFormatException e) {
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
            System.out.println("ISO_RECEIVED: " + HexFormatter.toHexString(data));
            try {
                IsoData isoData = isoFormatter.decode(data);
                System.out.println(isoData.toString());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }

            communicationListener.onFinish(true);
        });
    }

    private byte[] receive(Socket sc) throws IOException {
        InputStream input = sc.getInputStream();
        byte[] grossLength = input.readNBytes(2); // read data length
        int lengthData = ToolBox.bytesToInt(0, grossLength.length, grossLength); // unpack data length
        return input.readNBytes(lengthData); // read the length obtained
    }

    public interface CommunicationListener {

        void onFinish(boolean success);
    }
}