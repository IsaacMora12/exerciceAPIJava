package domain.port.exercise.in;

import domain.model.exercice.Muscle;

import java.util.List;

public interface UpdateMuscleUseCase {
    Muscle updateMuscle(Long id, String name, String description, List<String> images,
                        Boolean isActive, Long updatedBy);
}
