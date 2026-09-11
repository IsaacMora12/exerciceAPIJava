package domain.port.teams.in;

import domain.model.team.Invitation;

import java.time.LocalDateTime;

public interface SendInvitationUseCase {
    Invitation sendInvitation(Long teamId, String email, String role, LocalDateTime expiresAt);
}
