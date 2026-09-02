package infrastructure.adapter.in.web.teams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Payload required to update a role")
public record UpdateRoleRequest(
        @Schema(description = "Role name", example = "Admin")
        String name,

        @Schema(description = "Role permissions as JSON", example = "{\"can_edit\": true, \"can_delete\": true}")
        Map<String, Boolean> permissions
) {}
