package infrastructure.adapter.out.persistence.team;

import domain.model.team.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RolePersistenceMapper {

    private static final Logger log = LoggerFactory.getLogger(RolePersistenceMapper.class);

    // Domain -> ORM
    public static RoleEntity toEntity(Role domain) {
        if (domain == null) return null;
        RoleEntity entity = new RoleEntity(
                domain.getId(),
                domain.getTeamId(),
                domain.getName(),
                domain.getPermissions(),
                domain.getIsDefault(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
        log.debug("Mapping domain role to entity: id={}, name={}", domain.getId(), domain.getName());
        return entity;
    }

    // ORM -> Domain
    public static Role toDomain(RoleEntity entity) {
        if (entity == null) return null;
        log.debug("Mapping entity to domain role: id={}, name={}", entity.getId(), entity.getName());
        return Role.reconstruct(
                entity.getId(),
                entity.getTeamId(),
                entity.getName(),
                entity.getPermissions(),
                entity.getIsDefault(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
