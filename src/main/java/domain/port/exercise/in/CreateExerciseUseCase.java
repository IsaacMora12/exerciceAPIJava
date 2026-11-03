package domain.port.exercise.in;

import domain.model.exercise.Exercise;

import java.util.List;
import java.util.Map;

public interface CreateExerciseUseCase {
    Exercise createExercise(String name, Map<String, String> description, String category,
                            Map<String, List<String>> instruccion, String equipament,
                            Long mainMuscle, List<Long> othersMuscle,
                            List<String> images, List<String> videos, Long updatedBy);
}
