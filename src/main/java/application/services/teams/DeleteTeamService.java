package application.services.teams;

import domain.port.teams.TeamRepository;
import domain.port.teams.in.DeleteTeamUseCase;
import org.springframework.stereotype.Service;

@Service
public class DeleteTeamService implements DeleteTeamUseCase {

    private final TeamRepository teamRepository;

    public DeleteTeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public void deleteTeam(Long id) {
        if (teamRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Team not found");
        }
        teamRepository.deleteById(id);
    }
}
