package domain.port.auth.in;

public record LoginResult(String accessToken, String refreshToken) {}
