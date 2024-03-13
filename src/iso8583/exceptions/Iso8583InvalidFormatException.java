package iso8583.exceptions;

public class Iso8583InvalidFormatException extends Exception {
    /**
     * Constructs a <code>Iso8583FormatException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public Iso8583InvalidFormatException(String msg) {
        super(msg);
    }
}
