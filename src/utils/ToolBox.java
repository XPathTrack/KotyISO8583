package utils;

import java.nio.ByteBuffer;

public class ToolBox {

    private ToolBox() {
    }
    public static int bytesBase255ToIntBase10(byte[] base255, int position, int length) {
        ByteBuffer buffer = ByteBuffer.allocate(4); // Init buffer with the size in bytes of an int
        buffer.position(4 - length); // left padding for useless bytes
        for (int i = position; i < length; i++) {
            buffer.put(base255[i]);
        }
        buffer.position(0);
        return buffer.getInt();
    }

}
