package iso8583.data_class;

import iso8583.formatters.HexFormatter;

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

    @Override
    public String toString() {
        return toString(new StringBuilder());
    }

    public String toString(StringBuilder builder) {
        builder.append("TPDU: ").append(tpdu).append("\n");
        builder.append("MTI: ");
        HexFormatter.toHexString(mti, builder);
        builder.append("\n");
        builder.append("BITMAP: ");
        HexFormatter.toHexString(bitmap, builder);
        builder.append("\n");
        for (int i = 2; i < 64; i++) {
            if (fields[i] == null)
                continue;
            builder.append("FIELD ").append(i).append(": ").append(fields[i]).append("\n");
        }
        return builder.toString();
    }
}