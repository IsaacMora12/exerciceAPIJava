package application.services.auth;

import domain.port.auth.in.LogoutUseCase;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class LogoutService implements LogoutUseCase {

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
