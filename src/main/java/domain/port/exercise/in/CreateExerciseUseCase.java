package domain.port.exercise.in;

import domain.model.exercice.Exercise;

import java.util.List;

public interface CreateExerciseUseCase {
    Exercise createExercise(String name, String description, Long mainMuscle,
                            List<Long> othersMuscle, List<String> images,
                            List<String> videos, Long updatedBy);
}
