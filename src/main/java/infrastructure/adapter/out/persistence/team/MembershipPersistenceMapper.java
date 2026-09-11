package infrastructure.adapter.out.persistence.team;

import domain.model.team.Membership;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MembershipPersistenceMapper {

    private static final Logger log = LoggerFactory.getLogger(MembershipPersistenceMapper.class);

    // Domain -> ORM
    public static MembershipEntity toEntity(Membership domain) {
        if (domain == null) return null;
        MembershipEntity entity = new MembershipEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getTeamId(),
                domain.getRole(),
                domain.getIsActive(),
                domain.getJoinedAt(),
                domain.getUpdatedAt()
        );
        log.debug("Mapping domain membership to entity: id={}, userId={}, teamId={}",
                domain.getId(), domain.getUserId(), domain.getTeamId());
        return entity;
    }

    // ORM -> Domain
    public static Membership toDomain(MembershipEntity entity) {
        if (entity == null) return null;
        log.debug("Mapping entity to domain membership: id={}, userId={}, teamId={}",
                entity.getId(), entity.getUserId(), entity.getTeamId());
        return Membership.reconstruct(
                entity.getId(),
                entity.getUserId(),
                entity.getTeamId(),
                entity.getRole(),
                entity.getIsActive(),
                entity.getJoinedAt(),
                entity.getUpdatedAt()
        );
    }
}
