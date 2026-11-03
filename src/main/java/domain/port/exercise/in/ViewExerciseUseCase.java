package domain.port.exercise.in;

import domain.model.exercise.Exercise;

import java.util.Optional;

public interface ViewExerciseUseCase {
    Optional<Exercise> viewExercise(Long id);

}
