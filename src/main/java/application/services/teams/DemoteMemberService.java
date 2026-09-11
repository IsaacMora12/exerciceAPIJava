package application.services.teams;

import domain.model.team.Team;
import domain.model.team.Membership;
import domain.port.teams.MembershipRepository;
import domain.port.teams.TeamRepository;
import domain.port.teams.in.DemoteMemberUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemoteMemberService implements DemoteMemberUseCase {

    private static final Logger log = LoggerFactory.getLogger(DemoteMemberService.class);

    private final MembershipRepository membershipRepository;
    private final TeamRepository teamRepository;

    public DemoteMemberService(MembershipRepository membershipRepository, TeamRepository teamRepository) {
        this.membershipRepository = membershipRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public Membership demoteMember(Long membershipId, Long requesterId) {
        // 1. Get the membership to demote
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Membership not found"));

        // 2. Get the team to check owner
        Team team = teamRepository.findById(membership.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        // 3. Verify requester is an admin in the same team
        Membership requesterMembership = membershipRepository.findByUserIdAndTeamId(requesterId, membership.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Requester is not a member of this team"));

        if (!Membership.ADMIN.equals(requesterMembership.getRole())) {
            throw new IllegalArgumentException("Only admins can demote members");
        }

        // 4. Check the current member is actually an admin
        if (!Membership.ADMIN.equals(membership.getRole())) {
            throw new IllegalStateException("This member is already a regular member");
        }

        // 5. RULE: Owner cannot be demoted
        if (team.getOwner().equals(membership.getUserId())) {
            throw new IllegalStateException("The team owner cannot be demoted");
        }

        // 6. RULE: Team must always have at least one admin
        // NOTE: In the current system this is unreachable because owner is always admin.
        // It exists as a safety edge case if the owner could ever lose admin status.
        List<Membership> adminMembers = membershipRepository.findByTeamIdAndIsActive(membership.getTeamId(), true)
                .stream()
                .filter(m -> Membership.ADMIN.equals(m.getRole()))
                .toList();

        if (adminMembers.size() <= 1) {
            throw new IllegalStateException("Team must have at least one admin. Cannot demote the last admin.");
        }

        // 7. Demote the member
        membership.demote();
        return membershipRepository.save(membership);
    }
}
