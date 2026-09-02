package domain.port.users.in;

import domain.model.user.User;

public interface UpdateUserUseCase {
    User updateUser(Long id, String name, String email, String password, Boolean isActive);
}
