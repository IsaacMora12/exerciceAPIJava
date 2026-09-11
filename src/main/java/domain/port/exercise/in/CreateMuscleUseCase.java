package domain.port.exercise.in;

import domain.model.exercice.Muscle;

import java.util.List;

public interface CreateMuscleUseCase {
    Muscle createMuscle(String name, String description, List<String> images, Long updatedBy);
}
