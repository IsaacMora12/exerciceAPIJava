package domain.port.exercise.in;

import domain.model.exercice.Muscle;

import java.util.Optional;

public interface ViewMuscleUseCase {
    Optional<Muscle> viewMuscle(long id);
}
