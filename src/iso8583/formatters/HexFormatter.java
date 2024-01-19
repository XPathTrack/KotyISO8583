package iso8583.formatters;

public class HexFormatter {

    public byte[] hexAsciiByteToDecAsciiByte(int position, int length, byte... hexData) {
        byte[] asciiData = new byte[length];
        for (int i = 0; i < length; i++) {
            byte currentByte = hexData[position + i]; // get hex byte


        }
        return null;
    }

    public static final String[] hexStrings;
    static String clase = "ISOUtil.java";

    static {
        hexStrings = new String[256];
        for (int i = 0; i < 256; i++) {
            StringBuilder d = new StringBuilder(2);
            char ch = Character.forDigit((byte) i >> 4 & 0x0F, 16);
            d.append(Character.toUpperCase(ch));
            ch = Character.forDigit((byte) i & 0x0F, 16);
            d.append(Character.toUpperCase(ch));
            hexStrings[i] = d.toString();
        }
    }

    public String hexString(byte[] b) {
        StringBuilder d = new StringBuilder(b.length * 2);
        for (byte aB : b) {
            d.append(hexStrings[(int) aB & 0xFF]);
        }
        return d.toString();
    }

    public byte[] hexAsciiByteToDecAsciiByte(byte... data) {
        return hexAsciiByteToDecAsciiByte(0, data.length, data);
    }

    public byte byteToHexByte(byte data) {
        return 0;
    }

    public byte[] byteToHexByte(int position, int length, byte... data) {
        return null;
    }

    public byte[] byteToHexByte(byte... data) {
        return byteToHexByte(0, data.length, data);
    }
}