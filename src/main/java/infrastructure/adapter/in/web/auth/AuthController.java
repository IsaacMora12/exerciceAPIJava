package infrastructure.adapter.in.web.auth;

import domain.port.auth.in.LoginResult;
import domain.port.auth.in.LoginUseCase;
import domain.port.auth.in.LogoutUseCase;
import domain.port.auth.in.RefreshTokenUseCase;
import infrastructure.adapter.in.web.auth.dto.LoginRequest;
import infrastructure.adapter.in.web.auth.dto.LoginResponse;
import infrastructure.adapter.in.web.auth.dto.RefreshTokenRequest;
import infrastructure.adapter.in.web.users.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(LoginUseCase loginUseCase, LogoutUseCase logoutUseCase,
                          RefreshTokenUseCase refreshTokenUseCase) {
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @Operation(
            summary = "Login",
            description = "Authenticate a user and return access + refresh tokens"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResult result = loginUseCase.login(request.email(), request.password());
            log.info("User authenticated successfully: email={}", request.email());
            return new ResponseEntity<>(new LoginResponse(result.accessToken(), result.refreshToken()), HttpStatus.OK);
        } catch (BadCredentialsException e) {
            log.warn("Failed authentication attempt: email={}", request.email());
            ErrorResponse error = new ErrorResponse("Invalid email or password", 401);
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(
            summary = "Refresh Token",
            description = "Exchange a valid refresh token for new access + refresh tokens"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            LoginResult result = refreshTokenUseCase.refresh(request.refreshToken());
            log.info("Tokens refreshed successfully");
            return new ResponseEntity<>(new LoginResponse(result.accessToken(), result.refreshToken()), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to refresh token: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(e.getMessage(), 401);
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(
            summary = "Logout",
            description = "Invalidate the current session. Client must also discard tokens."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        logoutUseCase.logout();
        log.info("User logged out successfully");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
