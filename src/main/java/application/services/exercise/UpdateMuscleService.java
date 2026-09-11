package application.services.exercise;

import domain.model.exercice.Muscle;
import domain.port.exercise.MuscleRepository;
import domain.port.exercise.in.UpdateMuscleUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UpdateMuscleService implements UpdateMuscleUseCase {

    private final MuscleRepository muscleRepository;

    public UpdateMuscleService(MuscleRepository muscleRepository) {
        this.muscleRepository = muscleRepository;
    }

    @Override
    public Muscle updateMuscle(Long id, String name, String description, List<String> images,
                               Boolean isActive, Long updatedBy) {
        Muscle existing = muscleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Muscle not found with id: " + id));

        Optional<Muscle> muscleWithName = muscleRepository.findByName(name);
        if (muscleWithName.isPresent() && !muscleWithName.get().getId().equals(id)) {
            throw new IllegalArgumentException("Muscle name duplicated: " + name);
        }

        existing.updateName(name);
        existing.updateDescription(description);
        existing.updateImages(images);
        existing.updateUpdatedBy(updatedBy);
        if (Boolean.TRUE.equals(isActive)) {
            existing.activate();
        } else {
            existing.deactivate();
        }

        return muscleRepository.save(existing);
    }
}
