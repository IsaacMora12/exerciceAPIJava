package domain.port.exercise.in;

import domain.model.exercise.Muscle;

import java.util.List;
import java.util.Optional;

public interface ViewMuscleUseCase {
    Optional<Muscle> viewMuscle(long id);
    List<Muscle> findAll();
}
