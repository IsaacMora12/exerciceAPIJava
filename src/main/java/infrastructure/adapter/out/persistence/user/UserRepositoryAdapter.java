package infrastructure.adapter.out.persistence.user;

import domain.model.user.User;
import domain.port.users.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public UserRepositoryAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserPersistenceMapper.toEntity(user);
        UserEntity savedEntity = springDataUserRepository.save(entity);
        return UserPersistenceMapper.toDomain(savedEntity);
    }
    @Override
    public List<User> findByRol(UserEntity.Rol rol) {
        return springDataUserRepository.findByRol(rol).stream()
                .map(UserPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return springDataUserRepository.findAll().stream()
                .map(UserPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByActive(Boolean isActive) {
        return springDataUserRepository.findByIsActive(isActive).stream()
                .map(UserPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springDataUserRepository.deleteById(id);
    }
}