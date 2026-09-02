package application.services.teams;

import domain.model.team.Membership;
import domain.port.teams.MembershipRepository;
import domain.port.teams.in.AddMemberUseCase;
import org.springframework.stereotype.Service;

@Service
public class AddMemberService implements AddMemberUseCase {

    private final MembershipRepository membershipRepository;

    public AddMemberService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Override
    public Membership addMember(Long userId, Long teamId, Long roleId) {
        if (membershipRepository.existsByUserIdAndTeamId(userId, teamId)) {
            throw new IllegalArgumentException("User is already a member of this team");
        }

        Membership membership = Membership.create(userId, teamId, roleId);
        return membershipRepository.save(membership);
    }
}
