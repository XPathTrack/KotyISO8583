package iso8583.data_class;

public class IsoFieldFormat {
    private final String id;
    private final IsoLVarFormat lVarFormat;
    private final String dataFormat;
    private final String dinamicDataType;

    public IsoFieldFormat(String id, IsoLVarFormat lVarFormat, String dataFormat, String dinamicDataType) {
        this.id = id;
        this.lVarFormat = lVarFormat;
        this.dataFormat = dataFormat;
        this.dinamicDataType = dinamicDataType;
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

    public String getDinamicDataType() {
        return dinamicDataType;
    }
}