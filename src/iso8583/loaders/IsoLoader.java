package iso8583.loaders;

import iso8583.constants.*;
import iso8583.constants.l_vars.ILengthBytesType;
import iso8583.constants.l_vars.ILengthFormat;
import iso8583.constants.l_vars.ILengthType;
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
    private final String IsoDefaultFormat = "#README\n" +
            "\t#Data grouped within single quotes (') mean fixed values.\n" +
            "\n" +
            "#GENERAL ELEMENTS OF THE FORMAT\n" +
            "\t#FIELD_ID'='LENGTH_TYPE','LENGTH_BINES','DATA_TYPE\n" +
            "\n" +
            "#FORMAT DETAILS\n" +
            "\t#0: FIELD_ID\n" +
            "\t\t#String that identifies the field to which the format described after the '=' will be assigned.\n" +
            "\t#1: LENGTH_TYPE\n" +
            "\t\t#STATIC\n" +
            "\t\t\t#the data will always have the same length.\n" +
            "\t\t#L..._VARS\n" +
            "\t\t\t#The data has a variable size and must be assigned the following format.\n" +
            "\t\t\t#FORMAT ELEMENTS\n" +
            "\t\t\t\t#L..._COUNT'_'FORMAT'_'LENGTH_TYPE\n" +
            "\t\t\t#L..._COUNT\n" +
            "\t\t\t\t#The total number of 'L' means how many bytes describe the length of the data.\n" +
            "\t\t\t\t#EXAMPLE\n" +
            "\t\t\t\t\t#We have an example data: 30203030.\n" +
            "\t\t\t\t\t#L\n" +
            "\t\t\t\t\t\t#length: 04. #data: 30203030.\n" +
            "\t\t\t\t\t#LL\n" +
            "\t\t\t\t\t\t#length: 0004. #data: 30203030.\n" +
            "\t\t\t\t\t#LLL\n" +
            "\t\t\t\t\t\t#length: 000004. #data: 30203030.\n" +
            "\t\t\t#FORMAT\n" +
            "\t\t\t\t#format in which the length bytes will travel.\n" +
            "\t\t\t\t#BCD\n" +
            "\t\t\t\t\t#Due to the nature of the BCD format, each byte can be represented up to a length of 255. Only the DECIMAL position notation will be used, the base 255 will not be included.\n" +
            "\t\t\t\t\t#EXAMPLE.\n" +
            "\t\t\t\t\t\t#We have to represent the length in 3 bytes and the length of the data is 17. Our result will be: 000023.\n" +
            "\t\t\t\t#ASC\n" +
            "\t\t\t\t\t#Each byte of length will represent one ASCII digit of the total length. Due to the format, the notation position of each byte will be DECIMAL, due to this, it simplifies understanding.\n" +
            "\t\t\t\t\t#EXAMPLE\n" +
            "\t\t\t\t\t\t#We have to represent the length in 3 bytes and the data length is 17. Our result will be: 004955.\n" +
            "\t\t\t#LENGTH_TYPE\n" +
            "\t\t\t\t#They indicate how length values should be interpreted.\n" +
            "\t\t\t\t#BIN\n" +
            "\t\t\t\t\t#Means that the decoded value is exactly the number of bytes used to store the length of the field.\n" +
            "\t\t\t\t#RAW\n" +
            "\t\t\t\t\t#It means that the value was taken before converting to bytes and will be double the actual value in bytes.\n" +
            "\t#2: LENGTH_BINES\n" +
            "\t\t#LENGTH_TYPE == STATIC\n" +
            "\t\t\t#LLENGTH_BINES is the fixed length that the field data will have.\n" +
            "\t\t#LENGTH_TYPE == L...VARS\n" +
            "\t\t\t#LENGTH_BINES means that the length in bytes of the field is fixed.\n" +
            "\t#3: DATA_TYPE\n" +
            "\t\t#Indicates the format in which the field data will travel.\n" +
            "\t\t#ASC\n" +
            "\t\t\t#Each character will be sent in its ascii table representation.\n" +
            "\t\t#BCD\n" +
            "\t\t\t#Each character will be sent in its BCD representation.\n" +
            "\t#4: DINAMIC_DATA\n" +
            "\t\t#Indicates the type of subelements that the field stores.\n" +
            "\t\t#NA\n" +
            "\t\t\t#The field does not have subelements.\n" +
            "\t\t#LTV\n" +
            "\t\t\t#The field will have elements encoded in LTV.\n" +
            "\t\t#TLV\n" +
            "\t\t\t#The field will have elements encoded in TLV.\n" +
            "\n" +
            "FIELD_NUM=64\n" +
            "TPDU=STATIC,5,BCD,NA\n" +
            "MTI=STATIC,2,ASC,NA\n" +
            "1=STATIC,8,ASC,NA\n" +
            "2=L_BCD_RAW,10,BCD,NA\n" +
            "3=STATIC,3,BCD,NA\n" +
            "4=STATIC,6,BCD,NA\n" +
            "5=STATIC,6,BCD,NA\n" +
            "6=STATIC,6,BCD,NA\n" +
            "7=STATIC,6,BCD,NA\n" +
            "8=STATIC,6,BCD,NA\n" +
            "9=STATIC,6,BCD,NA\n" +
            "10=STATIC,6,BCD,NA\n" +
            "11=STATIC,3,BCD,NA\n" +
            "12=STATIC,3,BCD,NA\n" +
            "13=STATIC,2,BCD,NA\n" +
            "14=STATIC,2,BCD,NA\n" +
            "15=STATIC,2,BCD,NA\n" +
            "16=STATIC,6,BCD,NA\n" +
            "17=STATIC,6,BCD,NA\n" +
            "18=STATIC,6,BCD,NA\n" +
            "19=STATIC,6,BCD,NA\n" +
            "20=STATIC,6,BCD,NA\n" +
            "21=STATIC,6,BCD,NA\n" +
            "22=STATIC,2,BCD,NA\n" +
            "23=STATIC,2,BCD,NA\n" +
            "24=STATIC,2,BCD,NA\n" +
            "25=STATIC,2,BCD,NA\n" +
            "26=STATIC,2,BCD,NA\n" +
            "27=STATIC,6,BCD,NA\n" +
            "28=STATIC,6,BCD,NA\n" +
            "29=STATIC,6,BCD,NA\n" +
            "30=STATIC,6,BCD,NA\n" +
            "31=STATIC,6,BCD,NA\n" +
            "32=LL_BCD_BIN,6,BCD,NA\n" +
            "33=STATIC,6,BCD,NA\n" +
            "34=STATIC,6,BCD,NA\n" +
            "35=L_BCD_RAW,24,BCD,NA\n" +
            "36=STATIC,60,BCD,NA\n" +
            "37=STATIC,12,ASC,NA\n" +
            "38=STATIC,6,ASC,NA\n" +
            "39=STATIC,2,ASC,NA\n" +
            "40=STATIC,12,ASC,NA\n" +
            "41=STATIC,8,ASC,NA\n" +
            "42=STATIC,15,ASC,NA\n" +
            "43=STATIC,6,BCD,NA\n" +
            "44=LL_ASC_BIN,13,BCD,NA\n" +
            "45=LL_ASC_RAW,76,ASC,NA\n" +
            "46=STATIC,6,BCD,NA\n" +
            "47=STATIC,6,BCD,NA\n" +
            "48=LL_BCD_RAW,322,ASC,NA\n" +
            "49=STATIC,3,ASC,NA\n" +
            "50=STATIC,6,BCD,NA\n" +
            "51=STATIC,6,BCD,NA\n" +
            "52=STATIC,8,ASC,NA\n" +
            "53=STATIC,8,BCD,NA\n" +
            "54=LL_BCD_BIN,120,ASC,NA\n" +
            "55=LL_BCD_BIN,512,ASC,TLV\n" +
            "56=STATIC,6,BCD,NA\n" +
            "57=LL_ASC_RAW,512,ASC,NA\n" +
            "58=LL_BCD_BIN,512,ASC,NA\n" +
            "59=LL_BCD_BIN,512,ASC,NA\n" +
            "60=LL_ASC_RAW,999,ASC,NA\n" +
            "61=LL_ASC_RAW,999,ASC,NA\n" +
            "62=LL_BCD_BIN,999,ASC,NA\n" +
            "63=LL_BCD_BIN,512,ASC,LTV\n" +
            "64=LL_ASC_BIN,65000,ASC,NA";
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

        isoFormat.setBitmapFormat(new IsoFieldFormat("BITMAP", makeLVarFormat("STATIC", "NA", "NA", isoFormat.getFieldNum() >> 3), IDataFormat.ASC, "NA")); // length / 8
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
        String dinamicData = values[3];
        if (!Arrays.asList(IDinamicDataType.asArray).contains(dinamicData))
            throw new Iso8583InvalidFormatException("El tipo de datos dinamicos del campo " + key + " es inesperado: " + dinamicData);
        return new IsoFieldFormat(key, lVarFormat, dataType, dinamicData);
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
                    if (!Arrays.asList(ILengthBytesType.asArray).contains(bytesFormat))
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
