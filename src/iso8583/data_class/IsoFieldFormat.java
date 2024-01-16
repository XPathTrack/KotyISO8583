package iso8583.data_class;

public class IsoFieldFormat {
    private final String id;
    private final String lengthType;
    private final int lengthBytes;
    private final String dataType;

    public IsoFieldFormat(String id, String lengthType, int lengthBytes, String dataType) {
        this.id = id;
        this.lengthType = lengthType;
        this.lengthBytes = lengthBytes;
        this.dataType = dataType;
    }

    public String getId() {
        return id;
    }

    public String getLengthType() {
        return lengthType;
    }

    public int getLengthBytes() {
        return lengthBytes;
    }

    public String getDataType() {
        return dataType;
    }
}