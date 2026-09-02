package domain.port.teams.in;

import domain.model.team.Membership;

public interface UpdateMemberRoleUseCase {
    Membership updateMemberRole(Long membershipId, Long newRoleId);
}
