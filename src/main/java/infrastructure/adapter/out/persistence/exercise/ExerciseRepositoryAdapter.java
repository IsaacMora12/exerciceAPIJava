package infrastructure.adapter.out.persistence.exercise;

import domain.model.exercice.Exercise;
import domain.port.exercise.ExerciseRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ExerciseRepositoryAdapter implements ExerciseRepository {

    private final SpringDataExerciseRepository springDataExerciseRepository;

    public ExerciseRepositoryAdapter(SpringDataExerciseRepository springDataExerciseRepository) {
        this.springDataExerciseRepository = springDataExerciseRepository;
    }

    @Override
    public Exercise save(Exercise exercise) {
        ExerciseEntity entity = ExercisePersistenceMapper.toEntity(exercise);
        ExerciseEntity savedEntity = springDataExerciseRepository.save(entity);
        return ExercisePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Exercise> findById(Long id) {
        return springDataExerciseRepository.findById(id)
                .map(ExercisePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Exercise> findByName(String name) {
        return springDataExerciseRepository.findByName(name)
                .map(ExercisePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Exercise> findByPrincipalMuscle(Long muscleId) {
        return springDataExerciseRepository.findByMainMuscle(muscleId)
                .map(ExercisePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Exercise> findByOthersMuscle(List<Long> muscleIds) {
        return springDataExerciseRepository.findAll().stream()
                .filter(entity -> entity.getOthersMuscle() != null)
                .filter(entity -> muscleIds.stream()
                        .anyMatch(id -> entity.getOthersMuscle().contains(id)))
                .findFirst()
                .map(ExercisePersistenceMapper::toDomain);
    }

    @Override
    public Boolean existByMuscleOrOtherMuscle(Long muscleId) {
        // Check 1: ¿Algún exercise tiene este muscleId como mainMuscle?
        if (springDataExerciseRepository.existsByMainMuscle(muscleId)) {
            return true;
        }

        // Check 2: ¿Algún exercise tiene este muscleId en su lista othersMuscle?
        return springDataExerciseRepository.findAll().stream()
                .anyMatch(entity -> entity.getOthersMuscle() != null
                        && entity.getOthersMuscle().contains(muscleId));
    }

    @Override
    public List<Exercise> findAll() {
        return springDataExerciseRepository.findAll().stream()
                .map(ExercisePersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Exercise> findByIsActive(Boolean isActive) {
        return springDataExerciseRepository.findByIsActive(isActive).stream()
                .map(ExercisePersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springDataExerciseRepository.deleteById(id);
    }
}
