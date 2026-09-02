package application.services.users;

import domain.model.user.User;
import domain.port.users.UserRepository;
import domain.port.users.in.ViewUserUseCase;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ViewUserService implements ViewUserUseCase {

    private final UserRepository userRepository;

    public ViewUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> viewUser(Long id) {
        return userRepository.findById(id);
    }
}

