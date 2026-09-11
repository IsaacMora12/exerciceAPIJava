package domain.port.teams.in;

import domain.model.team.Membership;

public interface AddMemberUseCase {
    Membership addMember(Long userId, Long teamId, String role);
}
