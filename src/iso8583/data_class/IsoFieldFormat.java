package iso8583.data_class;

public class IsoFieldFormat {
    private final String id;
    private final String lengthFormat;
    private final String lengthType;
    private final int lengthBytes;
    private final String dataFormat;

    public IsoFieldFormat(String id, String lengthType, String lengthFormat, int lengthBytes, String dataFormat) {
        this.id = id;
        this.lengthType = lengthType;
        this.lengthFormat = lengthFormat;
        this.lengthBytes = lengthBytes;
        this.dataFormat = dataFormat;
    }

    public String getId() {
        return id;
    }

    public String getLengthType() {
        return lengthType;
    }

    public String getLengthFormat() {
        return lengthFormat;
    }

    public int getLengthBytes() {
        return lengthBytes;
    }

    public String getDataFormat() {
        return dataFormat;
    }
}