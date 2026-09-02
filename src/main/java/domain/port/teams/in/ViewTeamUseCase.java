package domain.port.teams.in;

import domain.model.team.Team;

import java.util.Optional;

public interface ViewTeamUseCase {
    Optional<Team> viewTeam(Long id);
}
