package domain.port.users.in;

import domain.model.user.User;

import java.util.Optional;

public interface ViewUserUseCase {
    Optional<User> viewUser(Long id);
}
