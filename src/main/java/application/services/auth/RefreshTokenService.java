package application.services.auth;

import domain.port.auth.in.LoginResult;
import domain.port.auth.in.RefreshTokenUseCase;
import infrastructure.adapter.out.persistence.auth.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService implements RefreshTokenUseCase {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public RefreshTokenService(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public LoginResult refresh(String refreshToken) {
        // 1. Extraer email del refresh token
        String email = jwtService.extractEmail(refreshToken);

        // 2. Cargar el usuario
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        // 3. Validar que sea un refresh token y que no esté expirado
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        if (!jwtService.isvalid(refreshToken, userDetails)) {
            throw new IllegalArgumentException("Refresh token is invalid or expired");
        }

        // 4. Generar nuevos tokens
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return new LoginResult(newAccessToken, newRefreshToken);
    }
}
