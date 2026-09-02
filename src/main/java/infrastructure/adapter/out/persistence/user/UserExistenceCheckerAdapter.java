package infrastructure.adapter.out.persistence.user;

import domain.port.teams.UserExistenceChecker;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserExistenceCheckerAdapter implements UserExistenceChecker {

    private final SpringDataUserRepository springDataUserRepository;

    public UserExistenceCheckerAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public boolean existsById(Long userId) {
        return springDataUserRepository.existsById(userId);
    }

    @Override
    public Optional<String> findUserNameById(Long userId) {
        return springDataUserRepository.findById(userId)
                .map(UserEntity::getName);
    }
}
