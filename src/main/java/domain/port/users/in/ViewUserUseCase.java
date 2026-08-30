package domain.port.users.in;

import domain.model.User;

import java.util.Optional;

public interface ViewUserUseCase {
    Optional<User> viewUser(Long id);
}
