package iso8583.data_class;

import iso8583.formatters.HexFormatter;

import java.util.HashMap;

public class IsoData {
    private final int maxSize;
    private final boolean request;
    private byte[] rawData;
    private String tpdu;
    private byte[] mti;
    private byte[] bitmap;
    private final HashMap<String, String> fields = new HashMap<>();
    private final HashMap<String, DinamicFieldsData> dFields = new HashMap<>();

    public IsoData(boolean request, int maxSize) {
        this.request = request;
        this.maxSize = maxSize;
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

    public int getMaxSize() {
        return maxSize;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public void putField(int fieldNumber, String value) {
        if (fieldNumber < 2 || fieldNumber > maxSize)
            throw new IndexOutOfBoundsException();
        fields.put(String.valueOf(fieldNumber), value);
    }

    public String getField(int fieldNumber) {
        if (fieldNumber < 2 || fieldNumber > maxSize)
            throw new IndexOutOfBoundsException();
        return fields.get(String.valueOf(fieldNumber));
    }

    public void putDField(int fieldNumber, DinamicFieldsData dField) {
        if (fieldNumber < 2 || fieldNumber > maxSize)
            throw new IndexOutOfBoundsException();
        dFields.put(String.valueOf(fieldNumber), dField);
    }

    public DinamicFieldsData getDField(int fieldNumber) {
        if (fieldNumber < 2 || fieldNumber > maxSize)
            throw new IndexOutOfBoundsException();

        return dFields.get(String.valueOf(fieldNumber));
    }

    @Override
    public String toString() {
        return toString(new StringBuilder());
    }

    public String toString(StringBuilder builder) {
        builder.append("TPDU: ").append(tpdu);
        builder.append("\nMTI: ");
        HexFormatter.toHexString(mti, builder);
        builder.append("\nBITMAP: ");
        HexFormatter.toHexString(bitmap, builder);
        for (int i = 2; i < 64; i++) {
            if (fields.containsKey(String.valueOf(i)))
                builder.append("\nFIELD ").append(i).append(": ").append(fields.get(String.valueOf(i)));
            else if (dFields.containsKey(String.valueOf(i))) {
                builder.append("\n");
                dFields.get(String.valueOf(i)).toString(builder);
            }
        }
        return builder.toString();
    }
}