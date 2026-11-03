package infrastructure.adapter.out.persistence.exercise;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Persiste un {@code Map<String, List<String>>} (i18n) como JSON en la columna.
 * Usado por {@code ExerciseEntity.instruccion}: {"en": ["paso 1", "paso 2"], ...}.
 */
@Converter
public class MapStringListConverter implements AttributeConverter<Map<String, List<String>>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final TypeReference<Map<String, List<String>>> TYPE =
            new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(Map<String, List<String>> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot serialize instruccion map to JSON", e);
        }
    }

    @Override
    public Map<String, List<String>> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return MAPPER.readValue(dbData, TYPE);
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot deserialize instruccion JSON to map", e);
        }
    }
}
