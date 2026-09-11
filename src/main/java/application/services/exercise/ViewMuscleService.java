package application.services.exercise;

import domain.model.exercice.Muscle;
import domain.port.exercise.MuscleRepository;
import domain.port.exercise.in.ViewMuscleUseCase;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ViewMuscleService implements ViewMuscleUseCase {
    private final MuscleRepository muscleRepository;
    public ViewMuscleService (MuscleRepository muscleRepository)
    {
        this.muscleRepository = muscleRepository;
    }
    @Override
    public Optional<Muscle> viewMuscle(long id)
    {return muscleRepository.findById(id);}


}
