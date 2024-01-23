package iso8583.data_class;

public class IsoFormat {
    private int fieldNum;
    private IsoFieldFormat tpduFormat;
    private IsoFieldFormat mtiFormat;
    private IsoFieldFormat bitmapFormat;
    private IsoFieldFormat[] fieldFormats;

    public int getFieldNum() {
        return fieldNum;
    }

    public void setFieldNum(int fieldNum) {
        this.fieldNum = fieldNum;
    }

    public IsoFieldFormat getTpduFormat() {
        return tpduFormat;
    }

    public void setTpduFormat(IsoFieldFormat tpduFormat) {
        this.tpduFormat = tpduFormat;
    }

    public IsoFieldFormat getMtiFormat() {
        return mtiFormat;
    }

    public void setMtiFormat(IsoFieldFormat mtiFormat) {
        this.mtiFormat = mtiFormat;
    }

    public IsoFieldFormat getBitmapFormat() {
        return bitmapFormat;
    }

    public void setBitmapFormat(IsoFieldFormat bitmapFormat) {
        this.bitmapFormat = bitmapFormat;
    }

    public IsoFieldFormat[] getFieldFormats() {
        return fieldFormats;
    }

    public void setFieldFormats(IsoFieldFormat[] fieldFormats) {
        this.fieldFormats = fieldFormats;
    }

    public IsoFieldFormat getFieldFormat(int fieldNum) {
        return fieldFormats[fieldNum - 1];
    }

    public void setFieldFormat(int fieldNum, IsoFieldFormat fieldFormats) {
        this.fieldFormats[fieldNum - 1] = fieldFormats;
    }
}
