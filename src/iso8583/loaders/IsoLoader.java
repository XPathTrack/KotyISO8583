package iso8583.loaders;

import iso8583.constants.IDataFormat;
import iso8583.constants.ILengthBytesFormat;
import iso8583.constants.ILengthFormat;
import iso8583.constants.ILengthType;
import iso8583.data_class.IsoFieldFormat;
import iso8583.data_class.IsoFormat;
import iso8583.data_class.IsoLVarFormat;
import iso8583.exceptions.Iso8583InvalidFormatException;

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
            "#1: LENGTH TYPE\n" +
            "\t#STATIC\n" +
            "\t\t#LENGTH BYTES=STATIC LENGTH IN DATA BYTES\n" +
            "\t#L...\n" +
            "\t\t#The total of \"L\" indicates the bytes that are declared to specify the actual length of the data \n" +
            "#2: LENGTH BYTES=DATA BYTES MAX LENGTH\n" +
            "#3: DATA TYPE\n" +
            "\t#ASC\n" +
            "\t#BCD\n" +
            "FIELD_NUM=64\n" +
            "TPDU=STATIC,5,BCD\n" +
            "MTI=STATIC,2,ASC\n" +
            "1=STATIC,8,ASC\n" +
            "2=L_BCD_BYT,10,BCD\n" +
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
            "32=LL_BCD_BYT,6,BCD\n" +
            "33=STATIC,6,BCD\n" +
            "34=STATIC,6,BCD\n" +
            "35=L_BCD_DEC,24,BCD\n" +
            "36=STATIC,60,BCD\n" +
            "37=STATIC,12,ASC\n" +
            "38=STATIC,6,ASC\n" +
            "39=STATIC,2,ASC\n" +
            "40=STATIC,12,ASC\n" +
            "41=STATIC,8,ASC\n" +
            "42=STATIC,15,ASC\n" +
            "43=STATIC,6,BCD\n" +
            "44=LL_DEC_BYT,13,BCD\n" +
            "45=LL_DEC_DEC,76,ASC\n" +
            "46=STATIC,6,BCD\n" +
            "47=STATIC,6,BCD\n" +
            "48=LL_BCD_DEC,322,ASC\n" +
            "49=STATIC,3,ASC\n" +
            "50=STATIC,6,BCD\n" +
            "51=STATIC,6,BCD\n" +
            "52=STATIC,8,ASC\n" +
            "53=STATIC,8,BCD\n" +
            "54=LL_BCD_BYT,120,ASC\n" +
            "55=LL_BCD_BYT,512,ASC\n" +
            "56=STATIC,6,BCD\n" +
            "57=LL_DEC_DEC,512,ASC\n" +
            "58=LL_BCD_BYT,512,ASC\n" +
            "59=LL_BCD_BYT,512,ASC\n" +
            "60=LL_DEC_DEC,999,ASC\n" +
            "61=LL_DEC_DEC,999,ASC\n" +
            "62=LL_BCD_BYT,999,ASC\n" +
            "63=LL_BCD_BYT,512,ASC\n" +
            "64=LL_DEC_BYT,65000,ASC";
    private final File formatFile;
    private final IsoFormat isoFormat = new IsoFormat();

    public IsoLoader(String isoFormatDefaultPath) throws Iso8583InvalidFormatException {
        formatFile = new File(isoFormatDefaultPath);
        if (!formatFile.exists()) {
            String primaryError = "Archivo de formato ISO no existe.";
            try {
                checkDir(formatFile.getParentFile());
            } catch (IOException e) {
                throw new Iso8583InvalidFormatException(primaryError + e.getMessage());
            }
            try {
                formatFile.createNewFile();
            } catch (IOException e) {
                throw new Iso8583InvalidFormatException(primaryError + "No es posible crearlo: " + formatFile.getPath());
            }
            try {
                writeDefaultformat(formatFile);
            } catch (IOException e) {
                throw new Iso8583InvalidFormatException(primaryError + "No es posible guardar la configuracion por defecto: " + formatFile.getPath());
            }
            throw new Iso8583InvalidFormatException(primaryError + "Configuracion por defecto guardada: " + formatFile.getPath());
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

    public IsoFormat loadFormat() throws Iso8583InvalidFormatException {
        Properties format = new Properties();
        try {
            format.load(new FileReader(formatFile));
        } catch (IOException e) {
            throw new Iso8583InvalidFormatException("Ruta no accesible: " + formatFile.getPath());
        }
        try {
            loadFormats(format);
        } catch (Iso8583InvalidFormatException e) {
            try {
                writeDefaultformat(formatFile);
            } catch (IOException io) {
                throw new Iso8583InvalidFormatException(e.getMessage() + " " + io.getMessage());
            }
            throw new Iso8583InvalidFormatException(e.getMessage() + "Configuracion por defecto guardada: " + formatFile.getPath());
        }
        return isoFormat;
    }

    private void loadFormats(Properties format) throws Iso8583InvalidFormatException {
        try {
            isoFormat.setFieldNum(Integer.parseInt(format.get("FIELD_NUM").toString()));
        } catch (NumberFormatException e) {
            throw new Iso8583InvalidFormatException("El valor asignado a \"FIELD_NUM\" es inesperado: " + isoFormat.getFieldNum());
        }
        if ((isoFormat.getFieldNum() & 7) > 0) // is not a multiple of 8
            throw new Iso8583InvalidFormatException("La cantidad de campos especificada en FIELD_NUM es invalida: " + isoFormat.getFieldNum());

        isoFormat.setBitmapFormat(new IsoFieldFormat("BITMAP", makeLVarFormat("STATIC", "NA", "NA", isoFormat.getFieldNum() >> 3), IDataFormat.ASC)); // length / 8
        isoFormat.setTpduFormat(processFormat(format, "TPDU"));
        isoFormat.setMtiFormat(processFormat(format, "MTI"));
        isoFormat.setFieldFormats(new IsoFieldFormat[isoFormat.getFieldNum()]);
        for (int i = 1; i <= isoFormat.getFieldNum(); i++) {
            isoFormat.setFieldFormat(i, processFormat(format, String.valueOf(i)));
        }
    }

    private IsoLVarFormat makeLVarFormat(String type, String decoderFormat, String bytesFormat, int lBytes) {
        return new IsoLVarFormat(type, decoderFormat, bytesFormat, lBytes);
    }

    private IsoFieldFormat processFormat(Properties format, String key) throws Iso8583InvalidFormatException {
        Object rawProduct = format.get(key);
        // Verificar si el format crudo no está presente
        if (rawProduct == null) {
            throw new Iso8583InvalidFormatException("Se requiere configuracion de FIELD_NUM, TPDU, MTI y todos los campos que indique FIELD_NUM.");
        }
        // Dividir la cadena de valores del producto crudo
        String[] values = rawProduct.toString().split(",");

        IsoLVarFormat lVarFormat = processLVarFormat(key, values[0], values[1]);
        String dataType = values[2];

        if (!Arrays.asList(IDataFormat.asArray).contains(dataType))
            throw new Iso8583InvalidFormatException("El tipo de dato del campo " + key + " es inesperado: " + dataType);
        return new IsoFieldFormat(key, lVarFormat, dataType);
    }

    private IsoLVarFormat processLVarFormat(String key, String rawFormat, String lBytesStr) throws Iso8583InvalidFormatException {
        String[] lInfo = rawFormat.split("_");
        String lengthType = lInfo[0];
        String decoderFormat;
        String bytesFormat;
        switch (lInfo.length) {
            case 1:
                if (!ILengthType.STATIC.equals(lengthType))
                    throw new Iso8583InvalidFormatException("El tipo de longitud del campo " + key + " es inesperado: " + lengthType);
                decoderFormat = "NA";
                bytesFormat = "NA";
                break;
                case 3:
                    if (!lengthType.matches(ILengthType.L_BYTES + "+"))
                        throw new Iso8583InvalidFormatException("El tipo de longitud del campo " + key + " es inesperado: " + lengthType);
                    decoderFormat = lInfo[1];
                    if (!Arrays.asList(ILengthFormat.asArray).contains(decoderFormat))
                        throw new Iso8583InvalidFormatException("El formato de longitud del campo " + key + " es inesperado: " + decoderFormat);
                    bytesFormat = lInfo[2];
                    if (!Arrays.asList(ILengthBytesFormat.asArray).contains(bytesFormat))
                        throw new Iso8583InvalidFormatException("El formato de los bytes dinamicos de longitud del campo " + key + " es inesperado: " + bytesFormat);
                break;
            default:
                throw new Iso8583InvalidFormatException("El valor en el formato de la longitud del campo " + key + " es inesperado: " + rawFormat);
        }
        int lBytes;
        try {
            lBytes = Integer.parseInt(lBytesStr);
        } catch (NumberFormatException e) {
            throw new Iso8583InvalidFormatException("La cantidad de bytes dinamicos asignados al campo " + key + " es inesperado: " + lBytesStr);
        }
        return new IsoLVarFormat(lengthType,decoderFormat, bytesFormat, lBytes);
    }
}
