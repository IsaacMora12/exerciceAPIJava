package application.services.teams;

import domain.model.team.Role;
import domain.port.teams.RoleRepository;
import domain.port.teams.in.CreateRoleUseCase;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CreateRoleService implements CreateRoleUseCase {

    private final RoleRepository roleRepository;

    public CreateRoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role createRole(Long teamId, String name, Map<String, Boolean> permissions, Boolean isDefault) {
        if (roleRepository.findByTeamIdAndName(teamId, name).isPresent()) {
            throw new IllegalArgumentException("Role name already exists in this team");
        }

        Role role = Role.create(teamId, name, permissions, isDefault);
        return roleRepository.save(role);
    }
}
