package domain.port.teams;

import domain.model.team.Membership;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository {

    Membership save(Membership membership);

    Optional<Membership> findById(Long id);

    Optional<Membership> findByUserIdAndTeamId(Long userId, Long teamId);

    List<Membership> findByTeamId(Long teamId);

    List<Membership> findByUserId(Long userId);

    List<Membership> findByTeamIdAndIsActive(Long teamId, Boolean isActive);

    boolean existsByUserIdAndTeamId(Long userId, Long teamId);

    void deleteById(Long id);
}
