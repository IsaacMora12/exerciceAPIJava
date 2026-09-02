package infrastructure.adapter.out.persistence.team;

import domain.model.team.Invitation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvitationPersistenceMapper {

    private static final Logger log = LoggerFactory.getLogger(InvitationPersistenceMapper.class);

    // Domain -> ORM
    public static InvitationEntity toEntity(Invitation domain) {
        if (domain == null) return null;
        InvitationEntity entity = new InvitationEntity(
                domain.getId(),
                domain.getTeamId(),
                domain.getEmail(),
                domain.getRoleId(),
                domain.getToken(),
                InvitationEntity.InvitationStatus.valueOf(domain.getStatus().name()),
                domain.getExpiresAt(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
        log.debug("Mapping domain invitation to entity: id={}, email={}", domain.getId(), domain.getEmail());
        return entity;
    }

    // ORM -> Domain
    public static Invitation toDomain(InvitationEntity entity) {
        if (entity == null) return null;
        log.debug("Mapping entity to domain invitation: id={}, email={}", entity.getId(), entity.getEmail());
        return Invitation.reconstruct(
                entity.getId(),
                entity.getTeamId(),
                entity.getEmail(),
                entity.getRoleId(),
                entity.getToken(),
                Invitation.Status.valueOf(entity.getStatus().name()),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
