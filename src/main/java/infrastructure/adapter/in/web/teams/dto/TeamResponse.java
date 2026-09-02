package infrastructure.adapter.in.web.teams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Team response payload")
public record TeamResponse(
        @Schema(description = "Team ID", example = "1")
        Long id,

        @Schema(description = "Team name", example = "Engineering")
        String name,

        @Schema(description = "Team slug", example = "engineering")
        String slug,

        @Schema(description = "Team description", example = "Backend development team")
        String description,

        @Schema(description = "Team owner (user ID)", example = "1")
        Long owner,

        @Schema(description = "Team active status", example = "true")
        Boolean isActive,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {}
