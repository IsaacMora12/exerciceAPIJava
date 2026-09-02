package application.services.teams;

import domain.model.team.Team;
import domain.port.teams.TeamRepository;
import domain.port.teams.in.UpdateTeamUseCase;
import org.springframework.stereotype.Service;

@Service
public class UpdateTeamService implements UpdateTeamUseCase {

    private final TeamRepository teamRepository;

    public UpdateTeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public Team updateTeam(Long id, String name, String slug, String description, Long owner, Boolean isActive) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        if (name != null) {
            team.updateName(name);
        }
        if (slug != null) {
            String baseSlug = slug;
            int counter = 1;
            while (teamRepository.findBySlug(slug).isPresent()
                    && !teamRepository.findBySlug(slug).get().getId().equals(id)) {
                slug = baseSlug + counter;
                counter++;
            }
            team.updateSlug(slug);
        }
        if (description != null) {
            team.updateDescription(description);
        }

        return teamRepository.save(team);
    }
}
