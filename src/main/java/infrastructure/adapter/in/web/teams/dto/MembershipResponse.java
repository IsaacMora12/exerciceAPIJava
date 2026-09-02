package infrastructure.adapter.in.web.teams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Membership response payload")
public record MembershipResponse(
        @Schema(description = "Membership ID", example = "1")
        Long id,

        @Schema(description = "User ID", example = "1")
        Long userId,

        @Schema(description = "Team ID", example = "1")
        Long teamId,

        @Schema(description = "Role ID", example = "1")
        Long roleId,

        @Schema(description = "Is active member", example = "true")
        Boolean isActive,

        @Schema(description = "When the user joined")
        LocalDateTime joinedAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {}
