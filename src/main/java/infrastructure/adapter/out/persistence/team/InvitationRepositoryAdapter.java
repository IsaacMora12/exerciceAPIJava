package infrastructure.adapter.out.persistence.team;

import domain.model.team.Invitation;
import domain.port.teams.InvitationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InvitationRepositoryAdapter implements InvitationRepository {

    private final SpringDataInvitationRepository springDataInvitationRepository;

    public InvitationRepositoryAdapter(SpringDataInvitationRepository springDataInvitationRepository) {
        this.springDataInvitationRepository = springDataInvitationRepository;
    }

    @Override
    public Invitation save(Invitation invitation) {
        InvitationEntity entity = InvitationPersistenceMapper.toEntity(invitation);
        InvitationEntity savedEntity = springDataInvitationRepository.save(entity);
        return InvitationPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Invitation> findById(Long id) {
        return springDataInvitationRepository.findById(id)
                .map(InvitationPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Invitation> findByToken(String token) {
        return springDataInvitationRepository.findByToken(token)
                .map(InvitationPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Invitation> findByTeamIdAndEmailAndStatus(Long teamId, String email, Invitation.Status status) {
        return springDataInvitationRepository.findByTeamIdAndEmailAndStatus(
                        teamId, email, InvitationEntity.InvitationStatus.valueOf(status.name()))
                .map(InvitationPersistenceMapper::toDomain);
    }

    @Override
    public List<Invitation> findByTeamId(Long teamId) {
        return springDataInvitationRepository.findByTeamId(teamId).stream()
                .map(InvitationPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invitation> findByTeamIdAndStatus(Long teamId, Invitation.Status status) {
        return springDataInvitationRepository.findByTeamIdAndStatus(
                        teamId, InvitationEntity.InvitationStatus.valueOf(status.name())).stream()
                .map(InvitationPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springDataInvitationRepository.deleteById(id);
    }
}
