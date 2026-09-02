package domain.port.users.in;

import domain.model.user.User;

public interface CreateUserUseCase {
    User createUser(String name, String email, String password);
}
