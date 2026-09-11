package infrastructure.adapter.out.persistence.exercise;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataExerciseRepository extends JpaRepository<ExerciseEntity, Long> {

    Optional<ExerciseEntity> findById(Long id);

    Optional<ExerciseEntity> findByName(String name);

    List<ExerciseEntity> findAll();

    List<ExerciseEntity> findByIsActive(Boolean isActive);

    // Verifica si algún exercise usa este muscleId como mainMuscle
    boolean existsByMainMuscle(Long muscleId);

    // Busca exercise por mainMuscle
    Optional<ExerciseEntity> findByMainMuscle(Long muscleId);
}
