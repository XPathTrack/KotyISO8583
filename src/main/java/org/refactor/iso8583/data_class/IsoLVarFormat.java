package org.refactor.iso8583.data_class;

public class IsoLVarFormat {

    private final String type;
    private final String decoderFormat;
    private final String bytesFormat;
    private final int lBytes;

    public IsoLVarFormat(String type, String decoderFormat, String bytesFormat, int lBytes) {
        this.type = type;
        this.decoderFormat = decoderFormat;
        this.bytesFormat = bytesFormat;
        this.lBytes = lBytes;
    }

    public String getType() {
        return type;
    }

    public String getDecoderFormat() {
        return decoderFormat;
    }

    public String getBytesFormat() {
        return bytesFormat;
    }

    public int getlBytes() {
        return lBytes;
    }
}
