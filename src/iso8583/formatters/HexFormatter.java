package iso8583.formatters;

public class HexFormatter {

    public byte[] hexByte2DecByte(byte[] hexData, int position, int length) {
        byte[] asciiData = new byte[length];
        for (int i = 0; i < length; i++) {
            byte currentByte = hexData[position + i]; // get hex byte

        }
        return null;
    }

    public byte[] hexByte2DecByte(byte[] data) {
        return hexByte2DecByte(data, 0, data.length);
    }

    public byte[] asciiByte2HexByte(byte[] data, int position, int length) {
        return null;
    }

    public byte[] asciiByte2HexByte(byte[] data) {
        return hexByte2DecByte(data, 0, data.length);
    }
}
