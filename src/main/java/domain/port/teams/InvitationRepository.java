package domain.port.teams;

import domain.model.team.Invitation;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository {

    Invitation save(Invitation invitation);

    Optional<Invitation> findById(Long id);

    Optional<Invitation> findByToken(String token);

    Optional<Invitation> findByTeamIdAndEmailAndStatus(Long teamId, String email, Invitation.Status status);

    List<Invitation> findByTeamId(Long teamId);

    List<Invitation> findByTeamIdAndStatus(Long teamId, Invitation.Status status);

    void deleteById(Long id);
}
