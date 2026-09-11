package application.services.exercise;

import domain.model.exercice.Exercise;
import domain.port.exercise.ExerciseRepository;
import domain.port.exercise.in.ViewExerciseUseCase;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ViewExerciseService implements ViewExerciseUseCase {
    private final ExerciseRepository exerciseRepository;
    public ViewExerciseService(ExerciseRepository exerciseRepository)
    {
        this.exerciseRepository = exerciseRepository;
    }
    @Override
    public Optional<Exercise> viewExercise(Long id)
    {return exerciseRepository.findById(id);}



}
