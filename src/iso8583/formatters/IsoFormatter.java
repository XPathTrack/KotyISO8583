package iso8583.formatters;

import UI.MainUI;
import iso8583.constant.IDataFormat;
import iso8583.constant.ILengthFormat;
import iso8583.constant.ILengthType;
import iso8583.data_class.IsoData;
import iso8583.data_class.IsoFieldFormat;
import iso8583.data_class.IsoFormat;
import iso8583.loader.IsoLoader;
import utils.ToolBox;

import java.io.IOException;

/**
 * @author PathTrack
 */
public class IsoFormatter {
    private final String IsoFormatDefaultPath = MainUI.PROGRAM_PATH + "/Packager/ISOPackager.properties";
    private final BcdFormatter bcdFormatter = new BcdFormatter();
    private final HexFormatter hexFormatter = new HexFormatter();
    private IsoFormat isoFormat;
    private int position;

    public IsoFormatter() throws IOException {
        IsoLoader isoLoader = new IsoLoader(IsoFormatDefaultPath, bcdFormatter, hexFormatter);
        isoFormat = isoLoader.loadFormat();
    }

    public IsoData decode(byte[] data) throws IOException {
        IsoData isoData = new IsoData(isoFormat.getFieldNum());

        isoData.setTpdu(new String(decodeField(data, position, isoFormat.getTpduFormat())));

        isoData.setMti(decodeField(data, position, isoFormat.getMtiFormat()));

        isoData.setBitmap(decodeField(data, position, isoFormat.getBitmapFormat()));

        int[] fields = decodeBitmap(isoData.getBitmap());

        for (int fieldN : fields) {
            isoData.put(fieldN, new String(decodeField(data, position, isoFormat.getFieldFormat(fieldN))));
            System.out.println("Campo " + fieldN + ": " + isoData.get(fieldN));
        }

        return isoData;
    }

    private int[] decodeBitmap(byte[] bitmap) {
        int[] maxData = new int[isoFormat.getFieldNum()];              // create array with the maximum number of fields possible
        int fieldsFound = 0;                            // number of fields found and offset
        for (int i = 0; i < bitmap.length; i++) {       // byte loop from bitmap
            byte b = bitmap[i];
            for (int bit = 7; bit >= 0; bit--) {        // bytemap bit loopback
                int bitValue = 1 << bit;                // calculate bit value from bit position
                if ((b & bitValue) != 0) {              // The bit calculated is active
                    maxData[fieldsFound] = (i * 8) + (8 - bit); // (offset) + (bit position) = # field
                    fieldsFound++;                      // increase the number of fields found
                }
            }
        }

        if (fieldsFound == maxData.length)              // the bitmap used all possible fields
            return maxData;

        int[] realData = new int[fieldsFound];          // create an array with the number of actual fields
        System.arraycopy(maxData, 0, realData, 0, fieldsFound);
        return realData;
    }

    private byte[] decodeField(byte[] data, int position, IsoFieldFormat fieldFormat) throws IOException {
        int length;
        if (ILengthType.STATIC.equals(fieldFormat.getLengthType())) {
            length = fieldFormat.getLengthBytes();
        } else {
            int lBytes = fieldFormat.getLengthType().length();
            length = decodeLengthBytes(position, lBytes, data, fieldFormat.getLengthFormat()); // position, #L?, data
            if (length > fieldFormat.getLengthBytes())
                throw new IOException("La longitud del campo " + fieldFormat.getId() + " -> " + length + " excede el maximo establecido -> " + fieldFormat.getLengthBytes());
            this.position = position += lBytes;
        }
        this.position += length;

        if (IDataFormat.HEX.equals(fieldFormat.getDataFormat())) {
            return hexFormatter.hexAsciiByteToDecAsciiByte(position, length, data);
        } else if (IDataFormat.BCD.equals(fieldFormat.getDataFormat())) {
            return bcdFormatter.bcdByteToAsciiByte(position, length, data);
        } else {
            byte[] plainData = new byte[length];
            System.arraycopy(data, position, plainData, 0, length);
            return plainData;
        }
    }

    private int decodeLengthBytes(int position, int length, byte[] rawData, String format) {
        switch (format) {
            case ILengthFormat.ASC:
                return ToolBox.bytesToInt(position, length, rawData); // UNFINISH
            case ILengthFormat.BYT:
                return ToolBox.bytesToInt(position, length, rawData); // array bytes to int
            default: // BCD
                byte[] bytesLongDec = bcdFormatter.bcdByteToDecByte(position, length, rawData); // bcd to decimal
                int intDec = ToolBox.bytesToInt(bytesLongDec); // array bytes to int
                return intDec >> 1;
        }

    }

    public byte[] encode(IsoData data) {
        byte[] maxData = new byte[estimateMaxSize()];

        return null;
    }

    private int estimateMaxSize() {
        int maxSize = isoFormat.getTpduFormat().getLengthBytes() + isoFormat.getMtiFormat().getLengthBytes() + isoFormat.getBitmapFormat().getLengthBytes();
        for (IsoFieldFormat fieldFormat : isoFormat.getFieldFormats()) {
            maxSize += fieldFormat.getLengthBytes();
        }
        return maxSize;
    }
}