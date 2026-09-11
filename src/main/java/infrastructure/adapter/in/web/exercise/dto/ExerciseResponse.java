package infrastructure.adapter.in.web.exercise.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Exercise response payload")
public record ExerciseResponse(
        @Schema(description = "Exercise ID", example = "1")
        Long id,

        @Schema(description = "Exercise name", example = "Barbell Bicep Curl")
        String name,

        @Schema(description = "Exercise description", example = "Standing curl with barbell targeting biceps")
        String description,

        @Schema(description = "Main muscle ID", example = "1")
        Long mainMuscle,

        @Schema(description = "List of secondary muscle IDs", example = "[2, 3]")
        List<Long> othersMuscle,

        @Schema(description = "List of image URLs for the exercise")
        List<String> images,

        @Schema(description = "List of video URLs for the exercise")
        List<String> videos,

        @Schema(description = "Exercise active status", example = "true")
        Boolean isActive,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt,

        @Schema(description = "User ID who last updated this exercise", example = "1")
        Long updatedBy
) {}
