package application.services.teams;

import domain.model.team.Membership;
import domain.port.teams.MembershipRepository;
import domain.port.teams.in.UpdateMemberRoleUseCase;
import org.springframework.stereotype.Service;

@Service
public class UpdateMemberRoleService implements UpdateMemberRoleUseCase {

    private final MembershipRepository membershipRepository;

    public UpdateMemberRoleService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Override
    public Membership updateMemberRole(Long membershipId, Long newRoleId) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Membership not found"));

        membership.updateRoleId(newRoleId);
        return membershipRepository.save(membership);
    }
}
