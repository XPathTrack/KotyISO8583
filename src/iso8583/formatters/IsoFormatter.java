package iso8583.formatters;

import UI.MainUI;
import iso8583.constant.IDataFormat;
import iso8583.constant.ILengthFormat;
import iso8583.constant.ILengthType;
import iso8583.data_class.IsoData;
import iso8583.data_class.IsoFieldFormat;
import utils.ToolBox;

import java.io.*;
import java.util.*;

/**
 * @author PathTrack
 */
public class IsoFormatter {
    private final String ISOPackagerDefaultPath = MainUI.PROGRAM_PATH + "/Packager/ISOPackager.properties";
    private BcdFormatter bcdFormatter;
    private HexFormatter hexFormatter;
    private int fieldNum;
    private IsoFieldFormat tpduFormat;
    private IsoFieldFormat mtiFormat;
    private IsoFieldFormat bitmapFormat;
    private IsoFieldFormat[] fieldFormats;
    private int position;

    public IsoFormatter() throws IOException {
        File packagerFile = new File(ISOPackagerDefaultPath);
        if (!packagerFile.exists()) {
            checkDir(packagerFile.getParentFile());
            try {
                packagerFile.createNewFile();
            } catch (IOException e) {
                throw new IOException("Archivo de formato ISO no existe y no es posible crearlo: " + packagerFile.getPath());
            }
            try {
                writeDefaultPackager(packagerFile);
            } catch (IOException e) {
                throw new IOException("Archivo de formato ISO no existe. " + e.getMessage());
            }
        }

        Properties format = new Properties();
        format.load(new FileReader(packagerFile));
        try {
            loadPackager(format);
        } catch (IOException e) {
            try {
                writeDefaultPackager(packagerFile);
            } catch (IOException io) {
                throw new IOException(e.getMessage() + " " + io.getMessage());
            }
        }
    }

    public IsoData decode(byte[] data) {
        IsoData isoData = new IsoData(fieldNum);
        try {
            isoData.setTpdu(new String(decodeField(data, position, tpduFormat)));
        } catch (IOException e) {
            // return error
        }
        try {
            isoData.setMti(decodeField(data, position, mtiFormat));
        } catch (IOException e) {
            // return error
        }
        try {
            isoData.setBitmap(decodeField(data, position, bitmapFormat));
        } catch (IOException e) {
            // return error
        }

        int[] fields = decodeBitmap(isoData.getBitmap());

        for (int fieldN : fields) {
            try {
                isoData.put(fieldN, new String(decodeField(data, position, fieldFormats[fieldN - 1])));
            } catch (IOException e) {
                // return error
            }
            System.out.println("Campo " + fieldN + ": " + isoData.get(fieldN));
        }

        return isoData;
    }

    private int[] decodeBitmap(byte[] bitmap) {
        int[] maxData = new int[fieldNum];              // create array with the maximum number of fields possible
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
            length = decodeLongBytes(position, lBytes, data); // position, #L?, data
            if (!IDataFormat.ASC.equals(fieldFormat.getDataFormat()))
                length = length >> 1;
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

    private int decodeLongBytes(int position, int length, byte[] rawData) {
        byte[] bytesLongDec = bcdFormatter.bcdByteToDecByte(position, length, rawData); // bcd to decimal
        return ToolBox.bytesToIntDec(bytesLongDec); // array bytes to int
    }

    public byte[] encode(IsoData data) {
        byte[] maxData = new byte[estimateMaxSize()];

        return null;
    }

    private int estimateMaxSize() {
        int maxSize = tpduFormat.getLengthBytes() + mtiFormat.getLengthBytes() + bitmapFormat.getLengthBytes();
        for (IsoFieldFormat fieldFormat : fieldFormats) {
            maxSize += fieldFormat.getLengthBytes();
        }
        return maxSize;
    }

    private void loadPackager(Properties format) throws IOException {
        fieldNum = Integer.parseInt(format.get("FIELD_NUM").toString());
        if ((fieldNum & 7) > 0) // is not a multiple of 8
            throw new IOException("La cantidad de campos especificada en FIELD_NUM es invalida: " + fieldNum);
        bitmapFormat = new IsoFieldFormat("BITMAP", "STATIC", "NA", fieldNum >> 3, IDataFormat.ASC); // length / 8
        tpduFormat = processRawFormat(format, "TPDU");
        mtiFormat = processRawFormat(format, "MTI");
        fieldFormats = new IsoFieldFormat[fieldNum];
        for (int i = 0; i < fieldNum; i++) {
            fieldFormats[i] = processRawFormat(format, String.valueOf(i + 1));
        }
    }

    private IsoFieldFormat processRawFormat(Properties format, String key) throws IOException {
        Object rawProduct = format.get(key);
        // Verificar si el format bruto no está presente
        if (rawProduct == null) {
            Arrays.fill(fieldFormats, null);
            throw new IOException("Packager incompleto. Se requiere configuracion de FIELD_NUM, TPDU, MTI y todos los campos que indique FIELD_NUM.");
        }
        // Dividir la cadena de valores del producto bruto
        String[] values = rawProduct.toString().split(",");

        String[] lengthInfo = values[0].split("_");
        String lengthType = lengthInfo[0];
        String lengthFormat;
        if (lengthInfo.length == 1) {
            if (!ILengthType.STATIC.equals(lengthType))
                throw new IOException("El tipo de longitud del campo " + key + " es inesperado: " + lengthType);
            lengthFormat = "NA";
        } else {
            if (!lengthType.matches(ILengthType.L_BYTES + "+"))
                throw new IOException("El tipo de longitud del campo " + key + " es inesperado: " + lengthType);
            lengthFormat = lengthInfo[1];
            if (!Arrays.asList(ILengthFormat.asArray).contains(lengthFormat))
                throw new IOException("El formato de longitud del campo " + key + " es inesperado: " + lengthFormat);

        }
        String dataType = values[2];
        if (IDataFormat.HEX.equals(dataType)) {
            if (hexFormatter == null) {
                hexFormatter = new HexFormatter();
            }
        } else if (IDataFormat.BCD.equals(dataType)) {
            if (bcdFormatter == null) {
                bcdFormatter = new BcdFormatter();
            }
        } else if (!IDataFormat.ASC.equals(dataType)) {
            throw new IOException("El tipo de dato del campo " + key + " es inesperado: " + dataType);
        }
        return new IsoFieldFormat(key, lengthType, lengthFormat, Integer.parseInt(values[1]), dataType);
    }

    private void writeDefaultPackager(File packagerFile) throws IOException {
        try (FileWriter writer = new FileWriter(packagerFile)) {
            writer.write(ISODefaultFormat);
        } catch (IOException e) {
            throw new IOException("No es posible guardar la configuracion por defecto: " + packagerFile.getPath());
        }

        throw new IOException("Configuracion por defecto guardada: " + packagerFile.getPath());
    }

    private void writeDefaultPackager() throws IOException {
        writeDefaultPackager(new File(ISOPackagerDefaultPath));
    }

    private void checkDir(File file) {
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public int getFieldNum() {
        return fieldNum;
    }

    private final String ISODefaultFormat = "#FIELD ID=LENGTH TYPE, LENGTH BYTES, DATA TYPE\n" +
            "\n" +
            "#DESCRIPTION\n" +
            "#LENGTH TYPE\n" +
            "\t#STATIC\n" +
            "\t\t#LENGTH BYTES=STATIC LENGTH IN DATA BYTES\n" +
            "\t#L...\n" +
            "\t\t#The total of \"L\" indicates the bytes that are declared to specify the actual length of the data\n" +
            "\t\t#LENGTH BYTES=DATA BYTES MAX LENGTH\n" +
            "#DATA TYPE\n" +
            "\t#ASC\n" +
            "\t#BCD\n" +
            "\t#HEX\n" +
            "FIELD_NUM=64\n" +
            "TPDU=STATIC,5,BCD\n" +
            "MTI=STATIC,2,ASC\n" +
            "1=STATIC,8,HEX\n" +
            "2=LL_BCD,10,BCD\n" +
            "3=STATIC,3,BCD\n" +
            "4=STATIC,6,BCD\n" +
            "5=STATIC,6,BCD\n" +
            "6=STATIC,6,BCD\n" +
            "7=STATIC,6,BCD\n" +
            "8=STATIC,6,BCD\n" +
            "9=STATIC,6,BCD\n" +
            "10=STATIC,6,BCD\n" +
            "11=STATIC,3,BCD\n" +
            "12=STATIC,3,BCD\n" +
            "13=STATIC,2,BCD\n" +
            "14=STATIC,2,BCD\n" +
            "15=STATIC,2,BCD\n" +
            "16=STATIC,6,BCD\n" +
            "17=STATIC,6,BCD\n" +
            "18=STATIC,6,BCD\n" +
            "19=STATIC,6,BCD\n" +
            "20=STATIC,6,BCD\n" +
            "21=STATIC,6,BCD\n" +
            "22=STATIC,2,BCD\n" +
            "23=STATIC,2,BCD\n" +
            "24=STATIC,2,BCD\n" +
            "25=STATIC,2,BCD\n" +
            "26=STATIC,2,BCD\n" +
            "27=STATIC,6,BCD\n" +
            "28=STATIC,6,BCD\n" +
            "29=STATIC,6,BCD\n" +
            "30=STATIC,6,BCD\n" +
            "31=STATIC,6,BCD\n" +
            "32=LL_BCD,6,BCD\n" +
            "33=STATIC,6,BCD\n" +
            "34=STATIC,6,BCD\n" +
            "35=L_BCD,24,BCD\n" +
            "36=STATIC,60,BCD\n" +
            "37=STATIC,12,ASC\n" +
            "38=STATIC,6,ASC\n" +
            "39=STATIC,2,ASC\n" +
            "40=STATIC,12,ASC\n" +
            "41=STATIC,8,ASC\n" +
            "42=STATIC,15,ASC\n" +
            "43=STATIC,6,BCD\n" +
            "44=LL_RAW,13,BCD\n" +
            "45=LL_BYT,76,ASC\n" +
            "46=STATIC,6,BCD\n" +
            "47=STATIC,6,BCD\n" +
            "48=LL_RAW,322,HEX\n" +
            "49=STATIC,3,ASC\n" +
            "50=STATIC,6,BCD\n" +
            "51=STATIC,6,BCD\n" +
            "52=STATIC,8,HEX\n" +
            "53=STATIC,8,BCD\n" +
            "54=LL_RAW,120,HEX\n" +
            "55=LL_RAW,512,HEX\n" +
            "56=STATIC,6,BCD\n" +
            "57=LL_BYT,512,ASC\n" +
            "58=LL_RAW,512,HEX\n" +
            "59=LL_RAW,512,HEX\n" +
            "60=LL_BYT,999,ASC\n" +
            "61=LL_BYT,999,ASC\n" +
            "62=LL_RAW,999,ASC\n" +
            "63=LL_RAW,512,ASC\n" +
            "64=LL_BYT,65000,ASC";
}