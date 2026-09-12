package application.services.exercise;

import domain.model.exercice.Exercise;
import domain.port.exercise.ExerciseRepository;
import domain.port.exercise.in.UpdateExerciseUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UpdateExerciseService implements UpdateExerciseUseCase {

    private final ExerciseRepository exerciseRepository;

    public UpdateExerciseService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public Exercise updateExercise(Long id, String name, List<String> description, String category,
                                   List<String> instruccion, String equipament,
                                   Long mainMuscle, List<Long> othersMuscle,
                                   List<String> images, List<String> videos,
                                   Boolean isActive, Long updatedBy) {
        Exercise existing = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found with id: " + id));

        Optional<Exercise> exerciseWithName = exerciseRepository.findByName(name);
        if (exerciseWithName.isPresent() && !exerciseWithName.get().getId().equals(id)) {
            throw new IllegalArgumentException("Exercise name duplicated: " + name);
        }

        existing.updateName(name);
        existing.updateDescription(description);
        existing.updateCategory(category);
        existing.updateInstruccion(instruccion);
        existing.updateEquipament(equipament);
        existing.updateMainMuscle(mainMuscle);
        existing.updateOthersMuscle(othersMuscle);
        existing.updateImages(images);
        existing.updateVideos(videos);
        existing.updateUpdatedBy(updatedBy);
        if (Boolean.TRUE.equals(isActive)) {
            existing.activate();
        } else {
            existing.deactivate();
        }

        return exerciseRepository.save(existing);
    }
}
