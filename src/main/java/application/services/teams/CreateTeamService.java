package application.services.teams;

import domain.model.team.Membership;
import domain.model.team.Team;
import domain.port.teams.MembershipRepository;
import domain.port.teams.TeamRepository;
import domain.port.teams.UserExistenceChecker;
import domain.port.teams.in.CreateTeamUseCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateTeamService implements CreateTeamUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateTeamService.class);

    private final TeamRepository teamRepository;
    private final MembershipRepository membershipRepository;
    private final UserExistenceChecker userExistenceChecker;

    public CreateTeamService(TeamRepository teamRepository,
                             MembershipRepository membershipRepository,
                             UserExistenceChecker userExistenceChecker) {
        this.teamRepository = teamRepository;
        this.membershipRepository = membershipRepository;
        this.userExistenceChecker = userExistenceChecker;
    }

    @Override
    @Transactional
    public Team createTeam(String name, String slug, String description, Long owner) {
        if (owner == null) {
            throw new IllegalArgumentException("The owner must not be empty");
        }
        if (!userExistenceChecker.existsById(owner)) {
            throw new IllegalArgumentException("The owner user does not exist");
        }

        if (name == null || name.isBlank()) {
            name = userExistenceChecker.findUserNameById(owner)
                    .orElseThrow(() -> new IllegalArgumentException("Could not retrieve owner name"));
        }

        if (slug == null || slug.isBlank()) {
            slug = name;
        }

        String baseSlug = slug;
        int counter = 1;
        while (teamRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + counter;
            counter++;
        }

        Team newTeam = Team.create(name, slug, owner, description);
        Team savedTeam = teamRepository.save(newTeam);

        // Add owner as admin member
        Membership ownerMembership = Membership.create(owner, savedTeam.getId(), Membership.ADMIN);
        Membership savedMembership = membershipRepository.save(ownerMembership);

        // Validate that the membership was created correctly
        validateOwnerMembership(savedTeam.getId(), owner, savedMembership);

        log.info("Team created with owner as admin: teamId={}, ownerId={}", savedTeam.getId(), owner);
        return savedTeam;
    }

    private void validateOwnerMembership(Long teamId, Long ownerId, Membership membership) {
        if (membership == null) {
            throw new IllegalStateException("Failed to create owner membership");
        }
        if (!membership.getUserId().equals(ownerId)) {
            throw new IllegalStateException("Owner membership has wrong userId");
        }
        if (!membership.getTeamId().equals(teamId)) {
            throw new IllegalStateException("Owner membership has wrong teamId");
        }
        if (!Membership.ADMIN.equals(membership.getRole())) {
            throw new IllegalStateException("Owner membership does not have admin role");
        }
        if (!membership.getIsActive()) {
            throw new IllegalStateException("Owner membership is not active");
        }
    }

}
