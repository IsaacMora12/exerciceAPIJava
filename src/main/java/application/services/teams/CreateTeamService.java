package application.services.teams;

import domain.model.team.Team;
import domain.port.teams.TeamRepository;
import domain.port.teams.UserExistenceChecker;
import domain.port.teams.in.CreateTeamUseCase;

import org.springframework.stereotype.Service;

@Service
public class CreateTeamService implements CreateTeamUseCase {

    private final TeamRepository teamRepository;
    private final UserExistenceChecker userExistenceChecker;

    public CreateTeamService(TeamRepository teamRepository, UserExistenceChecker userExistenceChecker) {
        this.teamRepository = teamRepository;
        this.userExistenceChecker = userExistenceChecker;
    }

    @Override
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

        return teamRepository.save(newTeam);
    }

}
