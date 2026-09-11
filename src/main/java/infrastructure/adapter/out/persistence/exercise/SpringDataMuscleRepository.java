package infrastructure.adapter.out.persistence.exercise;


import domain.model.exercice.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataMuscleRepository extends JpaRepository<MuscleEntity, Long>
{

    Optional<MuscleEntity> findById(Long id);

    Optional<MuscleEntity> findByName(String name);

    List<MuscleEntity> findAll();

    List<MuscleEntity> findByIsActive(Boolean isActive);

}
