package domain.port.exercise.in;

import domain.model.exercice.Exercise;

import java.util.Optional;

public interface ViewExerciseUseCase {
    Optional<Exercise> viewExercise(Long id);

}
