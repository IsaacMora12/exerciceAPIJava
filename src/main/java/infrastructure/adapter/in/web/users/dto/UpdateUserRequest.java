package infrastructure.adapter.in.web.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload required to create a new user")
public record UpdateUserRequest(
        @Schema(description = "User full name", example = "John Doe")
        String name,

        @Schema(description = "User email address", example = "john.doe@example.com")
        String email,

        @Schema(description = "Account password", example = "SecurePassword123!")
        String password
) {}