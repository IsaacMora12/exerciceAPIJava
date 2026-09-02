package application.services.teams;

import domain.model.team.Team;
import domain.port.teams.TeamRepository;
import domain.port.teams.in.ViewTeamUseCase;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ViewTeamService implements ViewTeamUseCase {

    private final TeamRepository teamRepository;

    public ViewTeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public Optional<Team> viewTeam(Long id) {
        return teamRepository.findById(id);
    }
}
