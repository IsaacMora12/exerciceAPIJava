package infrastructure.adapter.in.web.teams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload required to create a new team")
public record CreateTeamRequest(
        @Schema(description = "Team name", example = "Engineering")
        String name,

        @Schema(description = "Team slug (unique identifier)", example = "engineering")
        String slug,

        @Schema(description = "Team description", example = "Backend development team")
        String description
) {}
