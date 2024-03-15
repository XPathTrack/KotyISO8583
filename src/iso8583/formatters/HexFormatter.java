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
        return toHexString(bytes, 0, bytes.length);
    }

    public static void toHexString(byte[] bytes, StringBuilder builder) {
        toHexString(bytes, 0, bytes.length, builder);
    }

    public static String toHexString(byte[] bytes, int position, int length) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        toHexString(bytes, position, length, builder);
        return builder.toString();
    }
    public static void toHexString(byte[] bytes, int position, int length, StringBuilder builder) {
        for (int i = position; i < position + length; i++) {
            builder.append(toHexString(bytes[i]));
        }
    }
}