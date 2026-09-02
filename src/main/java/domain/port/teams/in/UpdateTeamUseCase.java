package domain.port.teams.in;

import domain.model.team.Team;

public interface UpdateTeamUseCase {
    Team updateTeam(Long id, String name, String slug, String description, Long owner, Boolean isActive);
}
