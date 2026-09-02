package infrastructure.adapter.out.persistence.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataTeamRepository extends JpaRepository<TeamEntity, Long> {

    Optional<TeamEntity> findBySlug(String slug);

    List<TeamEntity> findByIsActive(Boolean isActive);
}
