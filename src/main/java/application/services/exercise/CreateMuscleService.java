package application.services.exercise;

import domain.model.exercice.Muscle;
import domain.port.exercise.MuscleRepository;
import domain.port.exercise.in.CreateMuscleUseCase;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreateMuscleService implements CreateMuscleUseCase {

    private final MuscleRepository muscleRepository;

    public CreateMuscleService(MuscleRepository muscleRepository) {
        this.muscleRepository = muscleRepository;
    }

    @Override
    public Muscle createMuscle(String name, String description, List<String> images, Long updatedBy) {
        if (muscleRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Muscle name duplicated: " + name);
        }

        Muscle newMuscle = Muscle.create(name, description, images, updatedBy);

        return muscleRepository.save(newMuscle);
    }
}
