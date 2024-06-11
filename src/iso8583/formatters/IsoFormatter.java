package iso8583.formatters;

import ui.MainUI;
import iso8583.constants.*;
import iso8583.constants.l_vars.ILengthBytesType;
import iso8583.constants.l_vars.ILengthFormat;
import iso8583.constants.l_vars.ILengthType;
import iso8583.data_class.*;
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
        IsoData isoData = new IsoData(true, isoFormat.getFieldNum());
        isoData.setRawData(data);

        isoData.setTpdu(new String(decodeField(data, position, isoFormat.getTpduFormat())));

        isoData.setMti(decodeField(data, position, isoFormat.getMtiFormat()));

        isoData.setBitmap(decodeField(data, position, isoFormat.getBitmapFormat()));

        int[] fields = decodeBitmap(isoData.getBitmap());

        for (int fieldN : fields) {
            IsoFieldFormat fieldFormat = isoFormat.getFieldFormat(fieldN);
            byte[] decodedBytes = decodeField(data, position, fieldFormat);
            switch (fieldFormat.getDinamicDataType()) {
                case IDinamicDataType.TLV:
                    isoData.putDField(fieldN, decodeTlvField(fieldFormat.getId(), decodedBytes));
                    break;
                case IDinamicDataType.LTV:
                    isoData.putDField(fieldN, decodeLtvField(fieldFormat.getId(), decodedBytes));
                    break;
                default://NA
                    isoData.putField(fieldN, new String(decodedBytes));
                    break;
            }
        }
        position = 0;
        byte[] maxData = new byte[estimateMaxSize()];
        try {
            encodeField(maxData, isoData.getTpdu(), isoFormat.getTpduFormat());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return isoData;
    }

    private DinamicFieldsData decodeTlvField(String id, byte[] data) throws IOException {
        DinamicFieldsData dinamicData = new DinamicFieldsData(id);
        dinamicData.setRawData(data);
        for (int i = 0; i < data.length; ) {
            //TAG
            int lengthTag = Character.toUpperCase(data[i + 1]) == 'F' ? 4 : 2;
            String tag = new String(data, i, lengthTag);
            i += lengthTag;
            //LENGTH
            int length = Integer.parseInt(new String(data, i, 2)) << 1;
            i += 2;
            //VALUE
            if (length < 1)
                throw new IOException("Logitud dinamica invalida");//unfinish
            String value = new String(data, i, length);
            i += length;
            dinamicData.put(tag, value);
        }
        return dinamicData;
    }

    private DinamicFieldsData decodeLtvField(String id, byte[] data) throws IOException {
        DinamicFieldsData dinamicData = new DinamicFieldsData(id);
        dinamicData.setRawData(data);
        for (int i = 0; i < data.length; ) {
            //LENGTH
            int length = ToolBox.decBytesToDecInt(bcdFormatter.bcdToDec(i, 2, data));
            i += 2;
            //TAG
            String tag = new String(data, i, 2);
            i += 2;
            length -= 2;
            //VALUE
            if (length < 1)
                throw new IOException("Logitud dinamica invalida");//unfinish
            String value = new String(data, i, length);
            i += length;

            dinamicData.put(tag, value);
        }
        return dinamicData;
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
            bytesLength = Integer.parseInt(new String(rawData, position, length));

        if (ILengthBytesType.RAW.equals(format.getBytesFormat()))
            bytesLength = (bytesLength + 1) >> 1;

        return bytesLength;
    }

    public byte[] encode(IsoData isoData) throws Exception {
        byte[] maxData = new byte[estimateMaxSize()];
        encodeField(maxData, isoData.getTpdu(), isoFormat.getTpduFormat());
        System.arraycopy(isoData.getMti(), 0, maxData, position, isoData.getMti().length);
        position += isoData.getMti().length;
        System.arraycopy(isoData.getBitmap(), 0, maxData, position, isoData.getBitmap().length);
        position += isoData.getBitmap().length;

        return maxData;
    }

    private void encodeField(byte[] dest, String rawData, IsoFieldFormat fieldFormat) throws Exception {
        //DATA
        byte[] bytesData;
        if (IDataFormat.BCD.equals(fieldFormat.getDataFormat())) {
            bytesData = bcdFormatter.strCharToBcdByte(rawData);
        } else {
            bytesData = rawData.getBytes();
        }
        System.arraycopy(bytesData, 0, dest, position, bytesData.length);
        position += bytesData.length;
        //LENGTH
        if (ILengthType.STATIC.equals(fieldFormat.getlVarFormat().getType())) {
            if (fieldFormat.getlVarFormat().getlBytes() != bytesData.length)
                throw new Exception("sida para ti");//unfinish
        } else {
            encodeLVarBytes(dest, rawData.length(), bytesData.length, fieldFormat.getlVarFormat());
        }
    }

    private void encodeLVarBytes(byte[] dest, int rawLength, int decodedLength, IsoLVarFormat lVarFormat) throws Exception {//UNFiNISH
        byte[] dLength;
        String typeLength = String.valueOf(ILengthBytesType.RAW.equals(lVarFormat.getBytesFormat())
                ? rawLength : decodedLength);
        if (ILengthFormat.BCD.equals(lVarFormat.getDecoderFormat())) {
            dLength = bcdFormatter.strCharToBcdByte(typeLength);
            if (dLength.length > lVarFormat.getType().length())
                throw new Exception("cancer para ti");//unfinish
        } else {
            if (typeLength.length() > lVarFormat.getType().length())
                throw new Exception("cancer para ti");//unfinish
            dLength = typeLength.getBytes();
        }

        if (ToolBox.decBytesToDecInt(dLength) > lVarFormat.getlBytes())
            throw new Exception("herpes para ti");

        System.arraycopy(dLength, 0, dest, position, dLength.length);
        position += dLength.length;
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