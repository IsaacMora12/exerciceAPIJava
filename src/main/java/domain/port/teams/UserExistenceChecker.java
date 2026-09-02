package domain.port.teams;

import java.util.Optional;

public interface UserExistenceChecker {
    boolean existsById(Long userId);
    Optional<String> findUserNameById(Long userId);
}
