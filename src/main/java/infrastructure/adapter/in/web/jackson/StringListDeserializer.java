package infrastructure.adapter.in.web.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deserializes both String and Array into List<String>.
 * Accepts: "text" → ["text"], ["a","b"] → ["a","b"], "" → []
 */
public class StringListDeserializer extends ValueDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) { 
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String value = p.getValueAsString();
            if (value == null || value.isBlank()) {
                return Collections.emptyList();
            }
            return List.of(value);
        }

        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return Collections.emptyList();
        }

        if (p.currentToken() == JsonToken.START_ARRAY) {
            List<String> list = new ArrayList<>();
            while (p.nextToken() != JsonToken.END_ARRAY) {
                if (p.currentToken() == JsonToken.VALUE_STRING) {
                    list.add(p.getValueAsString());
                } else if (p.currentToken() == JsonToken.VALUE_NULL) {
                    list.add(null);
                } else {
                    list.add(p.getValueAsString());
                }
            }
            return list;
        }

        return Collections.emptyList();
    }
}
