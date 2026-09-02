package infrastructure.adapter.out.persistence.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataRoleRepository extends JpaRepository<RoleEntity, Long> {

    List<RoleEntity> findByTeamId(Long teamId);

    Optional<RoleEntity> findByTeamIdAndName(Long teamId, String name);

    Optional<RoleEntity> findByTeamIdAndIsDefault(Long teamId, Boolean isDefault);
}
