package iso8583.formatters;

public class HexFormatter {
    /**
     * Non instantiable class
     */
    private HexFormatter() {}
    private static final String[] hexStrings;

    static {
        hexStrings = new String[256];
        for (int i = 0; i < 256; i++) {
            char[] hexChar = new char[2];
            hexChar[0] = Character.toUpperCase(Character.forDigit((byte) i >> 4 & 0x0F, 16));//upperNibble
            hexChar[1] = Character.toUpperCase(Character.forDigit((byte) i & 0x0F, 16));//lowerNibble
            hexStrings[i] = new String(hexChar);
        }
    }

    public static String toHexString(byte decimalByte) {
        return hexStrings[decimalByte & 0xFF];
    }

    public static String toHexString(byte[] bytes) {
        return toHexString(bytes, new StringBuilder(bytes.length * 2));
    }

    public static String toHexString(byte[] bytes, StringBuilder builder) {
        for (byte e : bytes) {
            builder.append(toHexString(e));
        }
        return builder.toString();
    }
}