package domain.port.teams.in;

import domain.model.team.Role;

import java.util.Map;

public interface CreateRoleUseCase {
    Role createRole(Long teamId, String name, Map<String, Boolean> permissions, Boolean isDefault);
}
