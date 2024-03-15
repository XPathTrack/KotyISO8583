package iso8583.data_class;

import java.util.HashMap;
import java.util.Set;

public class DinamicFieldsData {

    private final String key;
    private final HashMap<String, String> fields = new HashMap<>();

    public DinamicFieldsData(String key) {
        this.key = key;
    }

    public void put(String key, String value) {
        fields.put(key, value);
    }

    public String get(String key) {
        return fields.get(key);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder());
    }

    public String toString(StringBuilder builder) {
        builder.append("DINAMIC FIELD: ").append(key);
        if (fields.isEmpty()) {
            builder.append("\n\tempty field");
            return builder.toString();
        }
        Set<String> keys = fields.keySet();
        for (String key : keys) {
            builder.append("\n\tFIELD ").append(key).append(": ").append(fields.get(key));
        }
        return builder.toString();
    }
}
