package application.services.teams;

import domain.port.teams.RoleRepository;
import domain.port.teams.in.DeleteRoleUseCase;
import org.springframework.stereotype.Service;

@Service
public class DeleteRoleService implements DeleteRoleUseCase {

    private final RoleRepository roleRepository;

    public DeleteRoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void deleteRole(Long id) {
        if (roleRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Role not found");
        }
        roleRepository.deleteById(id);
    }
}
