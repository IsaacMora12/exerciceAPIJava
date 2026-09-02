package infrastructure.adapter.out.persistence.team;

import domain.model.team.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamPersistenceMapper {

    private static final Logger log = LoggerFactory.getLogger(TeamPersistenceMapper.class);

    // Domain -> ORM
    public static TeamEntity toEntity(Team domain) {
        if (domain == null) return null;
        TeamEntity entity = new TeamEntity(
                domain.getId(),
                domain.getName(),
                domain.getSlug(),
                domain.getDescription(),
                domain.getOwner(),
                domain.getIsActive(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
        log.debug("Mapping domain team to entity: id={}, name={}", domain.getId(), domain.getName());
        return entity;
    }

    // ORM -> Domain
    public static Team toDomain(TeamEntity entity) {
        if (entity == null) return null;
        log.debug("Mapping entity to domain team: id={}, name={}", entity.getId(), entity.getName());
        return Team.reconstruct(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getDescription(),
                entity.getOwner(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
