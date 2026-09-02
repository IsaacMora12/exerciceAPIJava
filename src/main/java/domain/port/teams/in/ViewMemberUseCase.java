package domain.port.teams.in;

import domain.model.team.Membership;

import java.util.Optional;

public interface ViewMemberUseCase {
    Optional<Membership> viewMember(Long id);
}
