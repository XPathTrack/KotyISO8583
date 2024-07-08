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
}