package infrastructure.adapter.in.web.exercise.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Payload required to update a muscle")
public record UpdateMuscleRequest(
        @Schema(description = "Muscle name", example = "Bicep")
        String name,

        @Schema(description = "Muscle description", example = "Front upper arm muscle")
        String description,

        @Schema(description = "List of image URLs for the muscle")
        List<String> images,

        @Schema(description = "Muscle active status", example = "true")
        Boolean isActive
) {}
