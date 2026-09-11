package domain.port.exercise;

import domain.model.exercice.Muscle;

import java.util.List;
import java.util.Optional;

public interface MuscleRepository {
    Muscle save(Muscle muscle);

    Optional<Muscle> findById(Long id);

    Optional<Muscle> findByName(String name);

    List<Muscle> findAll();

    List<Muscle> findByIsActive(Boolean isActive);

    Boolean isMuscleUsed(Long id);
    void deleteById(Long id);
}
