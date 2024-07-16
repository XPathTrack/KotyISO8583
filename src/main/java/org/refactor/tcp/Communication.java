package org.refactor.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.refactor.iso8583.data_class.IsoData;
import org.refactor.iso8583.exceptions.Iso8583InvalidFormatException;
import org.refactor.iso8583.formatters.HexFormatter;
import org.refactor.iso8583.formatters.IsoFormatter;
import org.refactor.tcp.data.Client;
import org.refactor.ui.MainFrame;
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
                //client.getTalkLog().add(isoData);
                if (MainFrame.delay > 0) {
                    try {
                        Thread.sleep(MainFrame.delay * 1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                send(client.getSocket(), "60000000000210303807000AC00202000000000000900000000046161149071600720000000037323131363433303934373230303030303031313137303030303030303036303730303031020639463333303330303030343039353035303030303030303030303946333730343132334244323339394631303037303630313041303341303238303039463236303845393132323635433935343445424145394633363032303243443832303232303030394330313030394130333234303731363946303230363030303030303030393030303946323730313830394633343033334630303030394631413032303630303546324130323036303039463645303432303730303030303834303741303030303030303033313031300169004238313932323033303637343431323334353637383930313233343536373839303132333435363738393000123932303932303136343434370003383830000432395042009232324150524F4241444120202020202020202020202020202020202020202020202020202020202020204150524F42414441202020202020202020202020202020202020202020202020202020202020202032324150524F42414441000438395042");
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

    private void send(Socket sc, String hexMsg) throws IOException {
        byte[] raw = ToolBox.hexToByteAscii(hexMsg);
        byte[] data = new byte[raw.length + 2];
        data[0] = (byte) (data.length >> 8);
        data[1] = (byte) data.length;
        System.arraycopy(raw, 0, data, 2, raw.length);
        sc.getOutputStream().write(data);
    }

    public interface CommunicationListener {

        void onFinish(boolean success);
    }
}