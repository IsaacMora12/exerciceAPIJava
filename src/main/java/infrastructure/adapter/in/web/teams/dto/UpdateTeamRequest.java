package infrastructure.adapter.in.web.teams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload required to update a team")
public record UpdateTeamRequest(
        @Schema(description = "Team name", example = "Engineering")
        String name,

        @Schema(description = "Team slug (unique identifier)", example = "engineering")
        String slug,

        @Schema(description = "Team description", example = "Fullstack development team")
        String description,

        @Schema(description = "Team owner (user ID)", example = "1")
        Long owner
) {}
