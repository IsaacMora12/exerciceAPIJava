package domain.port.teams.in;

import domain.model.team.Role;

import java.util.Optional;

public interface ViewRoleUseCase {
    Optional<Role> viewRole(Long id);
}
