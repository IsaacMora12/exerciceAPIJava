package application.services.teams;

import domain.model.team.Membership;
import domain.port.teams.MembershipRepository;
import domain.port.teams.in.ViewMemberUseCase;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ViewMemberService implements ViewMemberUseCase {

    private final MembershipRepository membershipRepository;

    public ViewMemberService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Override
    public Optional<Membership> viewMember(Long id) {
        return membershipRepository.findById(id);
    }
}
