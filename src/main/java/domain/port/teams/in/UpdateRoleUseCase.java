package domain.port.teams.in;

import domain.model.team.Role;

import java.util.Map;

public interface UpdateRoleUseCase {
    Role updateRole(Long id, String name, Map<String, Boolean> permissions);
}
