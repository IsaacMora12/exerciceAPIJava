package domain.port.users;

import domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    List<User> findByActive(Boolean isActive);

    void deleteById(Long id);
}
