package infrastructure.adapter.out.persistence.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataInvitationRepository extends JpaRepository<InvitationEntity, Long> {

    Optional<InvitationEntity> findByToken(String token);

    Optional<InvitationEntity> findByTeamIdAndEmailAndStatus(Long teamId, String email, InvitationEntity.InvitationStatus status);

    List<InvitationEntity> findByTeamId(Long teamId);

    List<InvitationEntity> findByTeamIdAndStatus(Long teamId, InvitationEntity.InvitationStatus status);
}
