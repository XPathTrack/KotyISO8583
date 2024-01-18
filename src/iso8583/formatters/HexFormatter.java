package iso8583.formatters;

public class HexFormatter {

    public byte[] hexAsciiByteToDecAsciiByte(int position, int length, byte... hexData) {
        byte[] asciiData = new byte[length];
        for (int i = 0; i < length; i++) {
            byte currentByte = hexData[position + i]; // get hex byte


        }
        return null;
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