package infrastructure.adapter.in.web.exercise.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Muscle response payload")
public record MuscleResponse(
        @Schema(description = "Muscle ID", example = "1")
        Long id,

        @Schema(description = "Muscle name", example = "Bicep")
        String name,

        @Schema(description = "Muscle description", example = "Front upper arm muscle")
        String description,

        @Schema(description = "List of image URLs for the muscle")
        List<String> images,

        @Schema(description = "Muscle active status", example = "true")
        Boolean isActive,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt,

        @Schema(description = "User ID who last updated this muscle", example = "1")
        Long updatedBy
) {}
