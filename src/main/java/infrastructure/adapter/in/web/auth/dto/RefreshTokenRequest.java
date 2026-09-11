package infrastructure.adapter.in.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload required to refresh tokens")
public record RefreshTokenRequest(
        @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken
) {}
