package infrastructure.adapter.in.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload required to authenticate a user")
public record LoginRequest(
        @Schema(description = "User email address", example = "john.doe@example.com")
        String email,

        @Schema(description = "Account password", example = "SecurePassword123!")
        String password
) {}
