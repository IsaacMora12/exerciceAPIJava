package infrastructure.adapter.in.web.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deserializes both Number/String and Array into List<Long>.
 * Accepts: 1 → [1], "1" → [1], [1,2] → [1,2], "" → []
 */
public class LongListDeserializer extends ValueDeserializer<List<Long>> {

    @Override
    public List<Long> deserialize(JsonParser p, DeserializationContext ctxt) {
        if (p.currentToken() == JsonToken.VALUE_NUMBER_INT || p.currentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
            return List.of(p.getValueAsLong());
        }

        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String value = p.getValueAsString();
            if (value == null || value.isBlank()) {
                return Collections.emptyList();
            }
            return List.of(Long.parseLong(value));
        }

        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return Collections.emptyList();
        }

        if (p.currentToken() == JsonToken.START_ARRAY) {
            List<Long> list = new ArrayList<>();
            while (p.nextToken() != JsonToken.END_ARRAY) {
                if (p.currentToken() == JsonToken.VALUE_NULL) {
                    list.add(null);
                } else {
                    list.add(p.getValueAsLong());
                }
            }
            return list;
        }

        return Collections.emptyList();
    }
}
