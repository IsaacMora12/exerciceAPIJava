package infrastructure.adapter.out.persistence.team;

import domain.model.team.Role;
import domain.port.teams.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RoleRepositoryAdapter implements RoleRepository {

    private final SpringDataRoleRepository springDataRoleRepository;

    public RoleRepositoryAdapter(SpringDataRoleRepository springDataRoleRepository) {
        this.springDataRoleRepository = springDataRoleRepository;
    }

    @Override
    public Role save(Role role) {
        RoleEntity entity = RolePersistenceMapper.toEntity(role);
        RoleEntity savedEntity = springDataRoleRepository.save(entity);
        return RolePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Role> findById(Long id) {
        return springDataRoleRepository.findById(id)
                .map(RolePersistenceMapper::toDomain);
    }

    @Override
    public List<Role> findByTeamId(Long teamId) {
        return springDataRoleRepository.findByTeamId(teamId).stream()
                .map(RolePersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Role> findByTeamIdAndName(Long teamId, String name) {
        return springDataRoleRepository.findByTeamIdAndName(teamId, name)
                .map(RolePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Role> findDefaultByTeamId(Long teamId) {
        return springDataRoleRepository.findByTeamIdAndIsDefault(teamId, true)
                .map(RolePersistenceMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        springDataRoleRepository.deleteById(id);
    }
}
