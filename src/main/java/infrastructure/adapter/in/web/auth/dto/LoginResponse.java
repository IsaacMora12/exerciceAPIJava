package infrastructure.adapter.in.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after successful authentication")
public record LoginResponse(
        @Schema(description = "JWT access token (short-lived, 15 minutes)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "JWT refresh token (long-lived, 7 days)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken
) {}
