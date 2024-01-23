package iso8583.loader;

import iso8583.constant.IDataFormat;
import iso8583.constant.ILengthFormat;
import iso8583.constant.ILengthType;
import iso8583.data_class.IsoFieldFormat;
import iso8583.data_class.IsoFormat;
import iso8583.formatters.BcdFormatter;
import iso8583.formatters.HexFormatter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class IsoLoader {
    private final String IsoDefaultFormat = "#FIELD ID=LENGTH TYPE, LENGTH BYTES, DATA TYPE\n" +
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
            "44=LL_STR,13,BCD\n" +
            "45=LL_BYT,76,ASC\n" +
            "46=STATIC,6,BCD\n" +
            "47=STATIC,6,BCD\n" +
            "48=LL_STR,322,HEX\n" +
            "49=STATIC,3,ASC\n" +
            "50=STATIC,6,BCD\n" +
            "51=STATIC,6,BCD\n" +
            "52=STATIC,8,HEX\n" +
            "53=STATIC,8,BCD\n" +
            "54=LL_STR,120,HEX\n" +
            "55=LL_STR,512,HEX\n" +
            "56=STATIC,6,BCD\n" +
            "57=LL_BYT,512,ASC\n" +
            "58=LL_STR,512,HEX\n" +
            "59=LL_STR,512,HEX\n" +
            "60=LL_BYT,999,ASC\n" +
            "61=LL_BYT,999,ASC\n" +
            "62=LL_STR,999,ASC\n" +
            "63=LL_STR,512,ASC\n" +
            "64=LL_BYT,65000,ASC";
    private final File formatFile;
    private final BcdFormatter bcdFormatter;
    private final HexFormatter hexFormatter;
    private final IsoFormat isoFormat = new IsoFormat();

    public IsoLoader(String isoFormatDefaultPath, BcdFormatter bcdFormatter, HexFormatter hexFormatter) throws IOException {
        this.bcdFormatter = bcdFormatter;
        this.hexFormatter = hexFormatter;
        formatFile = new File(isoFormatDefaultPath);
        if (!formatFile.exists()) {
            String primaryError = "Archivo de formato ISO no existe.";
            try {
                checkDir(formatFile.getParentFile());
            } catch (IOException e) {
                throw new IOException(primaryError + e.getMessage());
            }
            try {
                formatFile.createNewFile();
            } catch (IOException e) {
                throw new IOException(primaryError + "No es posible crearlo: " + formatFile.getPath());
            }
            try {
                writeDefaultformat(formatFile);
            } catch (IOException e) {
                throw new IOException(primaryError + "No es posible guardar la configuracion por defecto: " + formatFile.getPath());
            }
            throw new IOException(primaryError + "Configuracion por defecto guardada: " + formatFile.getPath());
        }
    }

    private void writeDefaultformat(File formatFile) throws IOException {
        try (FileWriter writer = new FileWriter(formatFile)) {
            writer.write(IsoDefaultFormat);
        }
    }

    private void checkDir(File file) throws IOException {
        if (!file.exists()) {
            if (!file.mkdir())
                throw new IOException("Ruta no accesible: " + formatFile.getPath());
        }
    }

    public IsoFormat loadFormat() throws IOException {
        Properties format = new Properties();
        format.load(new FileReader(formatFile));
        try {
            loadFormats(format);
        } catch (IOException e) {
            try {
                writeDefaultformat(formatFile);
            } catch (IOException io) {
                throw new IOException(e.getMessage() + " " + io.getMessage());
            }
            throw new IOException(e.getMessage() + "Configuracion por defecto guardada: " + formatFile.getPath());
        }
        return isoFormat;
    }

    private void loadFormats(Properties format) throws IOException {
        try {
            isoFormat.setFieldNum(Integer.parseInt(format.get("FIELD_NUM").toString()));
        } catch (NumberFormatException e) {
            throw new IOException("El valor asignado a \"FIELD_NUM\" es inesperado: " + isoFormat.getFieldNum());
        }
        if ((isoFormat.getFieldNum() & 7) > 0) // is not a multiple of 8
            throw new IOException("La cantidad de campos especificada en FIELD_NUM es invalida: " + isoFormat.getFieldNum());
        isoFormat.setBitmapFormat(new IsoFieldFormat("BITMAP", "STATIC", "NA", isoFormat.getFieldNum() >> 3, IDataFormat.ASC)); // length / 8
        isoFormat.setTpduFormat(processRawFormat(format, "TPDU"));
        isoFormat.setMtiFormat(processRawFormat(format, "MTI"));
        isoFormat.setFieldFormats(new IsoFieldFormat[isoFormat.getFieldNum()]);
        for (int i = 1; i <= isoFormat.getFieldNum(); i++) {
            isoFormat.setFieldFormat(i, processRawFormat(format, String.valueOf(i)));
        }
    }

    private IsoFieldFormat processRawFormat(Properties format, String key) throws IOException {
        Object rawProduct = format.get(key);
        // Verificar si el format crudo no está presente
        if (rawProduct == null) {
            throw new IOException("Format incompleto. Se requiere configuracion de FIELD_NUM, TPDU, MTI y todos los campos que indique FIELD_NUM.");
        }
        // Dividir la cadena de valores del producto crudo
        String[] values = rawProduct.toString().split(",");

        String[] lengthInfo = values[0].split("_");
        String lengthType = lengthInfo[0];
        String lengthFormat;
        int lengthBytes;
        String dataType = values[2];
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
        try {
            lengthBytes = Integer.parseInt(values[1]);
        } catch (NumberFormatException e) {
            throw new IOException("El valor asignado a \"LENGTH_BYTES\" del campo " + key + " es inesperado: " + values[1]);
        }

        if (!Arrays.asList(IDataFormat.asArray).contains(dataType))
            throw new IOException("El tipo de dato del campo " + key + " es inesperado: " + dataType);
        return new IsoFieldFormat(key, lengthType, lengthFormat, lengthBytes, dataType);
    }
}
