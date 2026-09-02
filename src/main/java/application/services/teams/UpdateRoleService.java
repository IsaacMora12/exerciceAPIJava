package application.services.teams;

import domain.model.team.Role;
import domain.port.teams.RoleRepository;
import domain.port.teams.in.UpdateRoleUseCase;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UpdateRoleService implements UpdateRoleUseCase {

    private final RoleRepository roleRepository;

    public UpdateRoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role updateRole(Long id, String name, Map<String, Boolean> permissions) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        if (name != null) {
            roleRepository.findByTeamIdAndName(role.getTeamId(), name)
                    .filter(r -> !r.getId().equals(id))
                    .ifPresent(r -> { throw new IllegalArgumentException("Role name already exists in this team"); });
            role.updateName(name);
        }
        if (permissions != null) {
            role.updatePermissions(permissions);
        }

        return roleRepository.save(role);
    }
}
