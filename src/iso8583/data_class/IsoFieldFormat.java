package iso8583.data_class;

public class IsoFieldFormat {
    private final String id;
    private final IsoLVarFormat lVarFormat;
    private final String dataFormat;

    public IsoFieldFormat(String id, IsoLVarFormat lVarFormat, String dataFormat) {
        this.id = id;
        this.lVarFormat = lVarFormat;
        this.dataFormat = dataFormat;
    }

    public String getId() {
        return id;
    }

    public IsoLVarFormat getlVarFormat() {
        return lVarFormat;
    }

    public String getDataFormat() {
        return dataFormat;
    }
}