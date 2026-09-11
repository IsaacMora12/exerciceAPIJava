package infrastructure.adapter.out.persistence.exercise;

import domain.model.exercice.Muscle;
import domain.port.exercise.ExerciseRepository;
import domain.port.exercise.MuscleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MuscleRepositoryAdapter implements MuscleRepository {

    private final SpringDataMuscleRepository springDataMuscleRepository;
    private final ExerciseRepository exerciseRepository;

    public MuscleRepositoryAdapter(SpringDataMuscleRepository springDataMuscleRepository,
                                   ExerciseRepository exerciseRepository) {
        this.springDataMuscleRepository = springDataMuscleRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public Muscle save(Muscle team) {
        MuscleEntity entity = MusclePersistenceMapper.toEntity(team);
        MuscleEntity savedEntity = springDataMuscleRepository.save(entity);
        return MusclePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Muscle> findById(Long id) {
        return springDataMuscleRepository.findById(id)
                .map(MusclePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Muscle> findByName(String name) {
        return  springDataMuscleRepository.findByName(name)
                .map(MusclePersistenceMapper::toDomain);
    }


    @Override
    public List<Muscle> findAll() {
        return springDataMuscleRepository.findAll().stream()
                .map(MusclePersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Muscle> findByIsActive(Boolean isActive) {
        return springDataMuscleRepository.findByIsActive(isActive).stream()
                .map(MusclePersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean isMuscleUsed(Long id) {
        return exerciseRepository.existByMuscleOrOtherMuscle(id);
    }

    @Override
    public void deleteById(Long id) {
        springDataMuscleRepository.deleteById(id);
    }
}
