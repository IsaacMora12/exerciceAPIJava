package application.services.users;

import domain.model.user.User;
import domain.port.PasswordHasher;
import domain.port.users.UserRepository;
import domain.port.users.in.UpdateUserUseCase;
import infrastructure.adapter.out.persistence.user.UserEntity;
import org.springframework.stereotype.Service;

@Service
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UpdateUserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public User updateUser(Long id, String name, String email, String password, Boolean isActive, UserEntity.Rol rol) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (name != null) {
            user.updateName(name);
        }
        if (email != null) {
            user.updateEmail(email);
        }
        if (password != null) {
            User.validatePasswordStrength(password);
            String hashedPassword = passwordHasher.hash(password);
            user.setPassword(hashedPassword);
        }
        if (isActive != null) {
            if (isActive) {
                user.activate();
            } else {
                user.deactivate();
            }
        }
        if (rol != null) {
            user.updateRol(rol);
        }

        return userRepository.save(user);
    }
}
