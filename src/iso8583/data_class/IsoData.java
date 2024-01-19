package iso8583.data_class;

public class IsoData {
    private String tpdu;
    private byte[] mti;
    private byte[] bitmap;
    private final String[] fields;

    public IsoData(int size) {
        fields = new String[size];
    }

    public String getTpdu() {
        return tpdu;
    }

    public void setTpdu(String tpdu) {
        this.tpdu = tpdu;
    }

    public byte[] getMti() {
        return mti;
    }

    public void setMti(byte[] mti) {
        this.mti = mti;
    }

    public byte[] getBitmap() {
        return bitmap;
    }

    public void setBitmap(byte[] bitmap) {
        this.bitmap = bitmap;
    }

    public int getSize() {
        return fields.length;
    }

    public void put(int fieldNumber, String value) {
        if (fieldNumber < 2 || fieldNumber > fields.length)
            throw new IndexOutOfBoundsException();
        fields[fieldNumber] = value;
    }

    public String get(int pos) {
        if (pos < 2 || pos > fields.length)
            throw new IndexOutOfBoundsException();
        return fields[pos];
    }
}