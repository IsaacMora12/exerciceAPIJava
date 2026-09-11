package domain.port.users.in;

import domain.model.user.User;
import infrastructure.adapter.out.persistence.user.UserEntity;

public interface CreateUserUseCase {
    User createUser(String name, String email, String password, UserEntity.Rol rol);
}
