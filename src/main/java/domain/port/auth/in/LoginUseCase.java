package domain.port.auth.in;

public interface LoginUseCase {
    LoginResult login(String email, String password);
}
