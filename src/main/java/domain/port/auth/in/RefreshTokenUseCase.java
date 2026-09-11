package domain.port.auth.in;

public interface RefreshTokenUseCase {
    LoginResult refresh(String refreshToken);
}
