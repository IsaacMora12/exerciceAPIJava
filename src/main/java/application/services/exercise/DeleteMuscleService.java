package application.services.exercise;

import domain.port.exercise.MuscleRepository;
import domain.port.exercise.in.DeleteMuscleUseCase;
import org.springframework.stereotype.Service;

@Service
public class DeleteMuscleService implements DeleteMuscleUseCase {

    private final MuscleRepository muscleRepository;

    public DeleteMuscleService(MuscleRepository muscleRepository) {
        this.muscleRepository = muscleRepository;
    }

    @Override
    public void deleteMuscle(Long id) {
        if (muscleRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Muscle not found");
        }

        if (Boolean.TRUE.equals(muscleRepository.isMuscleUsed(id))) {
            throw new IllegalArgumentException("This muscle is in use and cannot be deleted");
        }

        muscleRepository.deleteById(id);
    }
}
