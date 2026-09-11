package infrastructure.adapter.in.web.teams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Invitation response payload")
public record InvitationResponse(
        @Schema(description = "Invitation ID", example = "1")
        Long id,

        @Schema(description = "Team ID", example = "1")
        Long teamId,

        @Schema(description = "Invited email", example = "john.doe@example.com")
        String email,

        @Schema(description = "Role assigned", example = "member")
        String role,

        @Schema(description = "Invitation token")
        String token,

        @Schema(description = "Invitation status", example = "PENDING")
        String status,

        @Schema(description = "Expiration date")
        LocalDateTime expiresAt,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {}
