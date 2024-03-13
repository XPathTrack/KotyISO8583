package iso8583.formatters;

import UI.MainUI;
import iso8583.constants.IDataFormat;
import iso8583.constants.ILengthBytesFormat;
import iso8583.constants.ILengthFormat;
import iso8583.constants.ILengthType;
import iso8583.data_class.IsoData;
import iso8583.data_class.IsoFieldFormat;
import iso8583.data_class.IsoFormat;
import iso8583.data_class.IsoLVarFormat;
import iso8583.exceptions.Iso8583InvalidFormatException;
import iso8583.loaders.IsoLoader;
import utils.ToolBox;

import java.io.IOException;

/**
 * @author PathTrack
 */
public class IsoFormatter {
    private final String IsoFormatDefaultPath = MainUI.PROGRAM_PATH + "/Packager/ISOPackager.properties";
    private final BcdFormatter bcdFormatter = new BcdFormatter();
    private final IsoFormat isoFormat;
    private int position;

    public IsoFormatter() throws Iso8583InvalidFormatException {
        IsoLoader isoLoader = new IsoLoader(IsoFormatDefaultPath);
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
        if (ILengthType.STATIC.equals(fieldFormat.getlVarFormat().getType())) {
            length = fieldFormat.getlVarFormat().getlBytes();
        } else {
            IsoLVarFormat lVarFormat = fieldFormat.getlVarFormat();
            int lVarBytes = lVarFormat.getType().length();
            length = decodeLVarBytes(position, lVarBytes, data, lVarFormat); // position, #L?, data
            if (length > lVarFormat.getlBytes())
                throw new IOException("La longitud del campo " + fieldFormat.getId() + " -> " + length
                        + " excede el maximo establecido -> " + lVarFormat.getlBytes());
            this.position = position += lVarBytes;
        }
        this.position += length;

        if (IDataFormat.BCD.equals(fieldFormat.getDataFormat())) {
            return bcdFormatter.bcdToAscii(position, length, data);
        } else { // ascii
            byte[] asciiData = new byte[length];
            System.arraycopy(data, position, asciiData, 0, length);
            return asciiData;
        }
    }

    private int decodeLVarBytes(int position, int length, byte[] rawData, IsoLVarFormat format) {
        int bytesLength;
        if (ILengthFormat.BCD.equals(format.getDecoderFormat()))
            bytesLength = ToolBox.decBytesToDecInt(bcdFormatter.bcdToDec(position, length, rawData));
        else
            bytesLength = ToolBox.decBytesToDecInt(position, length, rawData);

        if (ILengthBytesFormat.DEC.equals(format.getBytesFormat()))
            bytesLength = bytesLength >> 1;

        return bytesLength;
    }

    public byte[] encode(IsoData data) {
        byte[] maxData = new byte[estimateMaxSize()];

        return null;
    }

    private int estimateMaxSize() {
        int maxSize = isoFormat.getTpduFormat().getlVarFormat().getlBytes() + isoFormat.getMtiFormat().getlVarFormat().getlBytes()
                + isoFormat.getBitmapFormat().getlVarFormat().getlBytes(); // tpdu lBytes + mti lBytes + bitmap lBytes
        for (IsoFieldFormat fieldFormat : isoFormat.getFieldFormats()) {
            maxSize += fieldFormat.getlVarFormat().getlBytes();
        }
        return maxSize;
    }
}