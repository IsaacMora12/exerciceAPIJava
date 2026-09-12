package infrastructure.adapter.in.web.exercise.dto;

import tools.jackson.databind.annotation.JsonDeserialize;
import infrastructure.adapter.in.web.jackson.LongListDeserializer;
import infrastructure.adapter.in.web.jackson.StringListDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Payload required to update an exercise")
public record UpdateExerciseRequest(
        @Schema(description = "Exercise name", example = "Barbell Bicep Curl")
        String name,

        @Schema(description = "Exercise description", example = "[\"Standing curl with barbell\", \"Keep elbows close\"]")
        @JsonDeserialize(using = StringListDeserializer.class)
        List<String> description,

        @Schema(description = "Exercise category", example = "Strength")
        String category,

        @Schema(description = "Step-by-step instructions", example = "[\"Stand upright\", \"Curl the barbell\"]")
        @JsonDeserialize(using = StringListDeserializer.class)
        List<String> instruccion,

        @Schema(description = "Required equipment", example = "Barbell")
        String equipament,

        @Schema(description = "Main muscle ID", example = "1")
        Long mainMuscle,

        @Schema(description = "List of secondary muscle IDs", example = "[2, 3]")
        @JsonDeserialize(using = LongListDeserializer.class)
        List<Long> othersMuscle,

        @Schema(description = "List of image URLs for the exercise")
        @JsonDeserialize(using = StringListDeserializer.class)
        List<String> images,

        @Schema(description = "List of video URLs for the exercise")
        @JsonDeserialize(using = StringListDeserializer.class)
        List<String> videos,

        @Schema(description = "Exercise active status", example = "true")
        Boolean isActive
) {}
