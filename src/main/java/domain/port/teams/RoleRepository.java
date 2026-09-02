package domain.port.teams;

import domain.model.team.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {

    Role save(Role role);

    Optional<Role> findById(Long id);

    List<Role> findByTeamId(Long teamId);

    Optional<Role> findByTeamIdAndName(Long teamId, String name);

    Optional<Role> findDefaultByTeamId(Long teamId);

    void deleteById(Long id);
}
