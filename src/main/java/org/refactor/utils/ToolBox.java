package org.refactor.utils;

import java.nio.ByteBuffer;

public class ToolBox {

    private ToolBox() {
    }

    public static int bytesToInt(int position, int length, byte... bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(4); // Init buffer with the size in bytes of an int
        buffer.position(4 - length); // left padding for useless bytes
        for (int i = 0; i < length; i++) {
            buffer.put(bytes[position + i]);
        }
        buffer.position(0);
        return buffer.getInt();
    }

    public static int bytesToInt(byte... bytes) {
        return bytesToInt(0, bytes.length, bytes);
    }

    public static int decBytesToDecInt(int position, int length, byte... bytes) {
        int result = 0;
        int factor = 1;
        for (int i = length - 1; i >= 0; i--) { // bytes loopback
            result += (bytes[position + i]) * (factor); // (back byte) * (notational factor)
            factor *= 100; // Increase factor for the nex loop
        }
        return result; // example: bytes: {1,20,7,4}, result: 1200704
    }

    public static int decBytesToDecInt(byte... bytes) {
        return decBytesToDecInt(0, bytes.length, bytes);
    }





    /**
     * Use it to change a bytes array obtained from a hexadecimal string to
     * array ascii bytes
     *
     * @param hexByte array of byte obtained from hexadecimal string
     * @param beginIndex position of the byte array indicating from where to
     * transform the bytes to ascii hexadecimal
     * @param endIndex position of the byte array that indicates where it ends
     * from transforming the bytes to ascii hexadecimal
     * @return an array of bytes with the transformation of the array(hexByte)
     * in hexadecimal ascii, the size of this array is determined with the
     * formula (beginIndex - endIndex) or null when:
     * <p>
     * - endIndex is greater that length hexByte</p>
     * <p>
     * - beginIndex is greater or equals that endIndex</p>
     * <p>
     * - the parity of beginIndex and endIndex is different</p>
     */
    public static byte[] hexToByteAscii(byte[] hexByte, int beginIndex, int endIndex) {
        if (endIndex > hexByte.length || beginIndex >= endIndex || beginIndex % 2 != endIndex % 2) {
            return null;
        }
        byte[] bytes = new byte[(endIndex - beginIndex) >> 1];
        for (int i = beginIndex; i < endIndex; i++) {
            int shift = i % 2 == 1 ? 0 : 4;
            bytes[i - beginIndex >> 1] |= Character.digit((char) hexByte[i], 16) << shift;
        }
        return bytes;
    }


    /**
     * Transform the hexadecimal in String to array ascii bytes
     *
     * @param hexStr string with the hexadecimals
     * @return array ascii bytes of string with hexadecimals
     */
    public static byte[] hexToByteAscii(String hexStr) {
        if (hexStr.length() % 2 == 1) {
            hexStr = "0" + hexStr;
        }
        return hexToByteAscii(hexStr.getBytes(), 0, hexStr.length());
    }
}