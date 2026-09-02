package infrastructure.adapter.out.persistence.team;

import domain.model.team.Membership;
import domain.port.teams.MembershipRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MembershipRepositoryAdapter implements MembershipRepository {

    private final SpringDataMembershipRepository springDataMembershipRepository;

    public MembershipRepositoryAdapter(SpringDataMembershipRepository springDataMembershipRepository) {
        this.springDataMembershipRepository = springDataMembershipRepository;
    }

    @Override
    public Membership save(Membership membership) {
        MembershipEntity entity = MembershipPersistenceMapper.toEntity(membership);
        MembershipEntity savedEntity = springDataMembershipRepository.save(entity);
        return MembershipPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Membership> findById(Long id) {
        return springDataMembershipRepository.findById(id)
                .map(MembershipPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Membership> findByUserIdAndTeamId(Long userId, Long teamId) {
        return springDataMembershipRepository.findByUserIdAndTeamId(userId, teamId)
                .map(MembershipPersistenceMapper::toDomain);
    }

    @Override
    public List<Membership> findByTeamId(Long teamId) {
        return springDataMembershipRepository.findByTeamId(teamId).stream()
                .map(MembershipPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Membership> findByUserId(Long userId) {
        return springDataMembershipRepository.findByUserId(userId).stream()
                .map(MembershipPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Membership> findByTeamIdAndIsActive(Long teamId, Boolean isActive) {
        return springDataMembershipRepository.findByTeamIdAndIsActive(teamId, isActive).stream()
                .map(MembershipPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIdAndTeamId(Long userId, Long teamId) {
        return springDataMembershipRepository.existsByUserIdAndTeamId(userId, teamId);
    }

    @Override
    public void deleteById(Long id) {
        springDataMembershipRepository.deleteById(id);
    }
}
