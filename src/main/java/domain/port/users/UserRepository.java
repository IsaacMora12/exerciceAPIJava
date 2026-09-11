package domain.port.users;

import domain.model.user.User;
import infrastructure.adapter.out.persistence.user.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    List<User> findByActive(Boolean isActive);
    List<User> findByRol(UserEntity.Rol rol);

    void deleteById(Long id);
}
