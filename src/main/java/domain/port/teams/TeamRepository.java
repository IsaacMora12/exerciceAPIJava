package domain.port.teams;

import domain.model.team.Team;

import java.util.List;
import java.util.Optional;

public interface TeamRepository {

    Team save(Team team);

    Optional<Team> findById(Long id);

    Optional<Team> findBySlug(String slug);

    List<Team> findAll();

    List<Team> findByActive(Boolean isActive);

    void deleteById(Long id);
}
