package infrastructure.adapter.in.web.teams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Payload required to create a new role")
public record CreateRoleRequest(
        @Schema(description = "Role name", example = "Admin")
        String name,

        @Schema(description = "Role permissions as JSON", example = "{\"can_edit\": true, \"can_delete\": false}")
        Map<String, Boolean> permissions,

        @Schema(description = "Is this the default role for new members", example = "false")
        Boolean isDefault
) {}
