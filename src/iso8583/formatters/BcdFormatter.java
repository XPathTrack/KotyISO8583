/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iso8583.formatters;

/**
 * @author PathTrack
 */
public class BcdFormatter {

    /**
     * Convert a char value to bcd value.<p></p>
     *
     * @param data the String that you want to convert to bcd.<p>
     *             The string must only have values between '1' - '9', 'A' - 'F' or 'a' - 'f'.
     * @return a byte array representing the bcd value of the String
     */
    public byte[] asciiCharToBCDByte(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Input string cannot be null.");
        }
        char[] charArray = data.toCharArray();
        int pad = charArray.length & 1; //pad left for odd length
        byte[] byteArray = new byte[(charArray.length + pad) >> 1]; //assign even bytes length
        for (int i = 0; i < charArray.length; i++) {
            int index = i + pad;
            byteArray[index >> 1] |= (byte) (asciiCharToBCDByte(charArray[i]) << ((index & 1) == 1 ? 0 : 4)); //Position each char in its respective byte
        }
        return byteArray;
    }

    /**
     * Convert a char value to bcd value.
     *
     * @param value the char that you want to convert to bcd.<p>
     *              The char has to be a value between '1' - '9', 'A' - 'F' or 'a' - 'f'.
     * @return an integer representing the bcd value of the char
     */
    public byte asciiCharToBCDByte(char value) {
        if (value >= '0' && value <= '9') {
            return (byte) (value - '0');
        } else if (value >= 'A' && value <= 'F') {
            return (byte) (10 + value - 'A');
        } else if (value >= 'a' && value <= 'f') {
            return (byte) (10 + value - 'a');
        } else {
            throw new IllegalArgumentException("Carácter no válido para convertir a BCD: " + value);
        }
    }

    public byte[] bcdByteToAsciiByte(byte[] bcdBytes, int position, int length) {
        byte[] data = new byte[length << 1];
        for (int i = 0; i < length; i++) {
            byte bcdByte = bcdBytes[position + i];
            data[(i << 1)] = (byte) (simpleBcdIntToIntAscii(bcdByte >> 4 & 0x0f)); // top nibble in flat number converted to ascii
            data[(i << 1) + 1] = (byte) (simpleBcdIntToIntAscii(bcdByte & 0xf)); // bottom bite in flat number converted to ascii
        }
        return data;
    }

    public byte[] bcdByteToAsciiByte(byte[] bcdBytes) {
        return bcdByteToAsciiByte(bcdBytes, 0, bcdBytes.length);
    }

    public byte[] bcdByteToDecByte(byte[] bcdBytes, int position, int length) {
        byte[] data = new byte[length];
        for (int i = position; i < length; i++) {
            byte bcdByte = bcdBytes[i];
            data[i] = (byte) (((bcdByte >> 4) * 10) + bcdByte & 0xf); // upper nibble in flat number raised to base 10 and add the lower nibble
        }
        return data;
    }

    public byte[] bcdByteToDecByte(byte[] bcdBytes) {
        return bcdByteToDecByte(bcdBytes, 0, bcdBytes.length);
    }

    private int simpleBcdIntToIntAscii(int value) {
        if (value < 0 || value > 15)
            throw new IllegalArgumentException("Carácter no válido para convertir a BCD: " + value);
        return value + (value < 10 ? '0' : 'A');
    }
}
