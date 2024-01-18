package utils;

import java.nio.ByteBuffer;

public class ToolBox {

    private ToolBox() {
    }
    public static int bytesToIntDec(int position, int length, byte... bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(4); // Init buffer with the size in bytes of an int
        buffer.position(4 - length); // left padding for useless bytes
        for (int i = 0; i < length; i++) {
            buffer.put(bytes[position + i]);
        }
        buffer.position(0);
        return buffer.getInt();
    }

    public static int bytesToIntDec(byte... bytes) {
        return bytesToIntDec(0, bytes.length, bytes);
    }

}
