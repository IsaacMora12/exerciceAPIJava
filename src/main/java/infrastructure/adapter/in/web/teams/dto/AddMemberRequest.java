package infrastructure.adapter.in.web.teams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload required to add a member to a team")
public record AddMemberRequest(
        @Schema(description = "User ID", example = "1")
        Long userId,

        @Schema(description = "Role (must be 'admin' or 'member')", example = "member")
        String role
) {}
