package infrastructure.adapter.in.web.teams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Role response payload")
public record RoleResponse(
        @Schema(description = "Role ID", example = "1")
        Long id,

        @Schema(description = "Team ID", example = "1")
        Long teamId,

        @Schema(description = "Role name", example = "Admin")
        String name,

        @Schema(description = "Role permissions")
        Map<String, Boolean> permissions,

        @Schema(description = "Is default role", example = "false")
        Boolean isDefault,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {}
