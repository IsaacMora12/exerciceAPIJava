package infrastructure.adapter.out.persistence.exercise;

import domain.model.exercice.Muscle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MusclePersistenceMapper {

    private static final Logger log = LoggerFactory.getLogger(MusclePersistenceMapper.class);

    // Domain -> ORM
    public static MuscleEntity toEntity(Muscle domain) {
        if (domain == null) return null;
        MuscleEntity entity = new MuscleEntity(
                domain.getId(),
                domain.getName(),
                domain.getDescription(),
                domain.getImages(),
                domain.getIsActive(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getUpdatedBy()
        );
        log.debug("Mapping domain muscle to entity: id={}, name={}", domain.getId(), domain.getName());
        return entity;
    }

    // ORM -> Domain
    public static Muscle toDomain(MuscleEntity entity) {
        if (entity == null) return null;
        log.debug("Mapping entity to domain muscle: id={}, name={}", entity.getId(), entity.getName());
        return Muscle.reconstruct(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getImages(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
