package infrastructure.adapter.out.persistence.team;

import domain.model.team.Team;
import domain.port.teams.TeamRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TeamRepositoryAdapter implements TeamRepository {

    private final SpringDataTeamRepository springDataTeamRepository;

    public TeamRepositoryAdapter(SpringDataTeamRepository springDataTeamRepository) {
        this.springDataTeamRepository = springDataTeamRepository;
    }

    @Override
    public Team save(Team team) {
        TeamEntity entity = TeamPersistenceMapper.toEntity(team);
        TeamEntity savedEntity = springDataTeamRepository.save(entity);
        return TeamPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Team> findById(Long id) {
        return springDataTeamRepository.findById(id)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Team> findBySlug(String slug) {
        return springDataTeamRepository.findBySlug(slug)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public List<Team> findAll() {
        return springDataTeamRepository.findAll().stream()
                .map(TeamPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Team> findByActive(Boolean isActive) {
        return springDataTeamRepository.findByIsActive(isActive).stream()
                .map(TeamPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springDataTeamRepository.deleteById(id);
    }
}
