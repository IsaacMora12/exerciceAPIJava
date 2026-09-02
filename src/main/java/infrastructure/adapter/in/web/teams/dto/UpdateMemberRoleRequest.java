package infrastructure.adapter.in.web.teams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload required to update member role")
public record UpdateMemberRoleRequest(
        @Schema(description = "New Role ID", example = "2")
        Long roleId
) {}
