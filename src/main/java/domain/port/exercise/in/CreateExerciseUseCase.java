package domain.port.exercise.in;

import domain.model.exercice.Exercise;

import java.util.List;

public interface CreateExerciseUseCase {
    Exercise createExercise(String name, List<String> description, String category,
                            List<String> instruccion, String equipament,
                            Long mainMuscle, List<Long> othersMuscle,
                            List<String> images, List<String> videos, Long updatedBy);
}
