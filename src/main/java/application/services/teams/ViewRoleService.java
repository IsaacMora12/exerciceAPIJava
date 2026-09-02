package application.services.teams;

import domain.model.team.Role;
import domain.port.teams.RoleRepository;
import domain.port.teams.in.ViewRoleUseCase;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ViewRoleService implements ViewRoleUseCase {

    private final RoleRepository roleRepository;

    public ViewRoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<Role> viewRole(Long id) {
        return roleRepository.findById(id);
    }
}
