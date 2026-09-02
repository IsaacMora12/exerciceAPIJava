package application.services.teams;

import domain.model.team.Invitation;
import domain.port.teams.InvitationRepository;
import domain.port.teams.in.SendInvitationUseCase;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SendInvitationService implements SendInvitationUseCase {

    private final InvitationRepository invitationRepository;

    public SendInvitationService(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Override
    public Invitation sendInvitation(Long teamId, String email, Long roleId, LocalDateTime expiresAt) {
        // Check if there's already a pending invitation for this email in this team
        invitationRepository.findByTeamIdAndEmailAndStatus(teamId, email, Invitation.Status.PENDING)
                .ifPresent(i -> { throw new IllegalArgumentException("A pending invitation already exists for this email"); });

        String token = UUID.randomUUID().toString();
        Invitation invitation = Invitation.create(teamId, email, roleId, token, expiresAt);
        return invitationRepository.save(invitation);
    }
}
