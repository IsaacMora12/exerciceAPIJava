package application.services.users;

import domain.port.PasswordHasher;
import domain.port.users.UserRepository;
import domain.model.user.User;
import domain.port.users.in.CreateUserUseCase;
import infrastructure.adapter.out.persistence.user.UserEntity;
import org.springframework.stereotype.Service;

@Service
public class CreateUserService implements CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public CreateUserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public User createUser(String name, String email, String password, UserEntity.Rol rol) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email duplicated");
        }

        User.validatePasswordStrength(password);
        String hashedPassword = passwordHasher.hash(password);

        UserEntity.Rol finalRol = (rol != null) ? rol : UserEntity.Rol.USER;
        User newUser = User.create(name, email, hashedPassword, finalRol);

        return userRepository.save(newUser);
    }
}
