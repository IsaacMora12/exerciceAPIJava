package application.services.teams;

import domain.model.team.Invitation;
import domain.port.teams.InvitationRepository;
import domain.port.teams.in.CancelInvitationUseCase;
import org.springframework.stereotype.Service;

@Service
public class CancelInvitationService implements CancelInvitationUseCase {

    private final InvitationRepository invitationRepository;

    public CancelInvitationService(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Override
    public void cancelInvitation(Long id) {
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        invitation.cancel();
        invitationRepository.save(invitation);
    }
}
