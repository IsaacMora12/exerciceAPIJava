package application.services.teams;

import domain.model.team.Invitation;
import domain.model.team.Membership;
import domain.port.teams.InvitationRepository;
import domain.port.teams.MembershipRepository;
import domain.port.teams.in.AcceptInvitationUseCase;
import org.springframework.stereotype.Service;

@Service
public class AcceptInvitationService implements AcceptInvitationUseCase {

    private final InvitationRepository invitationRepository;
    private final MembershipRepository membershipRepository;

    public AcceptInvitationService(InvitationRepository invitationRepository,
                                   MembershipRepository membershipRepository) {
        this.invitationRepository = invitationRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    public Membership acceptInvitation(String token, Long userId) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        invitation.accept();
        invitationRepository.save(invitation);

        if (membershipRepository.existsByUserIdAndTeamId(userId, invitation.getTeamId())) {
            throw new IllegalArgumentException("User is already a member of this team");
        }

        Membership membership = Membership.create(userId, invitation.getTeamId(), invitation.getRole());
        return membershipRepository.save(membership);
    }
}
