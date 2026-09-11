package infrastructure.adapter.in.web.teams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Payload required to send an invitation")
public record SendInvitationRequest(
        @Schema(description = "Email to invite", example = "john.doe@example.com")
        String email,

        @Schema(description = "Role to assign (must be 'admin' or 'member')", example = "member")
        String role,

        @Schema(description = "Expiration date", example = "2026-09-30T23:59:59")
        LocalDateTime expiresAt
) {}
