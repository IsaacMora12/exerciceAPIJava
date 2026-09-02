package infrastructure.adapter.out.persistence.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataMembershipRepository extends JpaRepository<MembershipEntity, Long> {

    Optional<MembershipEntity> findByUserIdAndTeamId(Long userId, Long teamId);

    List<MembershipEntity> findByTeamId(Long teamId);

    List<MembershipEntity> findByUserId(Long userId);

    List<MembershipEntity> findByTeamIdAndIsActive(Long teamId, Boolean isActive);

    boolean existsByUserIdAndTeamId(Long userId, Long teamId);
}
