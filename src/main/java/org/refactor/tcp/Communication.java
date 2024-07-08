package org.refactor.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.refactor.iso8583.data_class.IsoData;
import org.refactor.iso8583.exceptions.Iso8583InvalidFormatException;
import org.refactor.iso8583.formatters.HexFormatter;
import org.refactor.iso8583.formatters.IsoFormatter;
import org.refactor.tcp.data.Client;
import org.refactor.utils.TaskThread;
import org.refactor.utils.ToolBox;

/**
 * @author PathTrack
 */
public class Communication {

    private static final Communication instance = new Communication();

    private IsoFormatter isoFormatter;
    private final TaskThread taskExecutor = new TaskThread();

    private Communication() {
    }

    public static Communication getInstance() {
        return instance;
    }

    public void prepare() throws Iso8583InvalidFormatException {
        isoFormatter = new IsoFormatter();
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
                System.out.println("DECODED ISO -> {\n" + isoData.toString() + "\n}");
                client.getTalkLog().add(isoData);
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