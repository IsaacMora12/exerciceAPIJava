package infrastructure.adapter.out.persistence.exercise;

import domain.model.exercice.Exercise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExercisePersistenceMapper {

    private static final Logger log = LoggerFactory.getLogger(ExercisePersistenceMapper.class);

    // Domain -> ORM
    public static ExerciseEntity toEntity(Exercise domain) {
        if (domain == null) return null;
        ExerciseEntity entity = new ExerciseEntity(
                domain.getId(),
                domain.getName(),
                domain.getDescription(),
                domain.getMainMuscle(),
                domain.getOthersMuscle(),
                domain.getImages(),
                domain.getVideos(),
                domain.getIsActive(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getUpdatedBy()
        );
        log.debug("Mapping domain exercise to entity: id={}, name={}", domain.getId(), domain.getName());
        return entity;
    }

    // ORM -> Domain
    public static Exercise toDomain(ExerciseEntity entity) {
        if (entity == null) return null;
        log.debug("Mapping entity to domain exercise: id={}, name={}", entity.getId(), entity.getName());
        return Exercise.reconstruct(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getMainMuscle(),
                entity.getOthersMuscle(),
                entity.getImages(),
                entity.getVideos(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
