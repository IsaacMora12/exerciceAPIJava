package application.services.teams;

import domain.port.teams.MembershipRepository;
import domain.port.teams.in.RemoveMemberUseCase;
import org.springframework.stereotype.Service;

@Service
public class RemoveMemberService implements RemoveMemberUseCase {

    private final MembershipRepository membershipRepository;

    public RemoveMemberService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Override
    public void removeMember(Long membershipId) {
        if (membershipRepository.findById(membershipId).isEmpty()) {
            throw new IllegalArgumentException("Membership not found");
        }
        membershipRepository.deleteById(membershipId);
    }
}
