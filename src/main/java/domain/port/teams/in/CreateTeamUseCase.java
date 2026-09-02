package domain.port.teams.in;

import domain.model.team.Team;


public interface CreateTeamUseCase {

    Team createTeam(String name, String slug, String description, Long owner);
}
