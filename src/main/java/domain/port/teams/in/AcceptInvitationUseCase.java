package domain.port.teams.in;

import domain.model.team.Membership;

public interface AcceptInvitationUseCase {
    Membership acceptInvitation(String token, Long userId);
}
