package application.services.exercise;

import domain.port.exercise.ExerciseRepository;
import domain.port.exercise.in.DeleteExerciseUseCase;
import org.springframework.stereotype.Service;

@Service
public class DeleteExerciseService implements DeleteExerciseUseCase {

    private final ExerciseRepository exerciseRepository;

    public DeleteExerciseService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public void deleteExercise(Long id) {
        if (exerciseRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Exercise not found with id: " + id);
        }

        exerciseRepository.deleteById(id);
    }
}
