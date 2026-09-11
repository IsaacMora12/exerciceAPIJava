package application.services.teams;

import domain.model.team.Membership;
import domain.port.teams.MembershipRepository;
import domain.port.teams.in.RemoveMemberUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RemoveMemberService implements RemoveMemberUseCase {

    private static final Logger log = LoggerFactory.getLogger(RemoveMemberService.class);

    private final MembershipRepository membershipRepository;

    public RemoveMemberService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Override
    public void removeMember(Long membershipId) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Membership not found"));

        // If removing an admin, check that team will still have at least one admin
        if (Membership.ADMIN.equals(membership.getRole())) {
            List<Membership> adminMembers = membershipRepository.findByTeamIdAndIsActive(membership.getTeamId(), true)
                    .stream()
                    .filter(m -> Membership.ADMIN.equals(m.getRole()))
                    .toList();

            if (adminMembers.size() <= 1) {
                throw new IllegalStateException("Cannot remove the last admin from the team");
            }
        }

        membershipRepository.deleteById(membershipId);
        log.info("Member removed: membershipId={}, userId={}, teamId={}",
                membership.getId(), membership.getUserId(), membership.getTeamId());
    }
}
