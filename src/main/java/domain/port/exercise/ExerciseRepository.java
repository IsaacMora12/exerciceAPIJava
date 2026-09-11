package domain.port.exercise;

import domain.model.exercice.Exercise;

import java.util.List;
import java.util.Optional;

public interface ExerciseRepository {
    Exercise save(Exercise exercise);

    Optional<Exercise> findById(Long id);

    Optional<Exercise> findByName(String name);

    Optional<Exercise> findByPrincipalMuscle(Long muscleId);
    Optional<Exercise> findByOthersMuscle(List<Long> muscleId);
    Boolean existByMuscleOrOtherMuscle(Long muscleId);
    List<Exercise> findAll();

    List<Exercise> findByIsActive(Boolean isActive);

    void deleteById(Long id);

}
