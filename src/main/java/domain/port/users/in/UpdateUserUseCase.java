package domain.port.users.in;

import domain.model.user.User;
import infrastructure.adapter.out.persistence.user.UserEntity;

public interface UpdateUserUseCase {
    User updateUser(Long id, String name, String email, String password, Boolean isActive, UserEntity.Rol rol);
}
