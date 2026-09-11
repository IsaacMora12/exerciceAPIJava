package application.services.exercise;

import domain.model.exercice.Exercise;
import domain.port.exercise.ExerciseRepository;
import domain.port.exercise.in.CreateExerciseUseCase;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreateExerciseService implements CreateExerciseUseCase {

    private final ExerciseRepository exerciseRepository;

    public CreateExerciseService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public Exercise createExercise(String name, String description, Long mainMuscle,
                                   List<Long> othersMuscle, List<String> images,
                                   List<String> videos, Long updatedBy) {
        if (exerciseRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Exercise name duplicated: " + name);
        }

        Exercise newExercise = Exercise.create(name, description, mainMuscle,
                othersMuscle, images, videos, updatedBy);

        return exerciseRepository.save(newExercise);
    }
}
