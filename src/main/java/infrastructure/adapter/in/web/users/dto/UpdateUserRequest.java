package infrastructure.adapter.in.web.users.dto;

import infrastructure.adapter.out.persistence.user.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload required to update a user")
public record UpdateUserRequest(
        @Schema(description = "User full name", example = "John Doe")
        String name,

        @Schema(description = "User email address", example = "john.doe@example.com")
        String email,

        @Schema(description = "Account password", example = "SecurePassword123!")
        String password,

        @Schema(description = "User Status", example = "True")
        Boolean isActive,

        @Schema(description = "User role: USER or ADMIN", example = "USER", allowableValues = {"USER", "ADMIN"})
        UserEntity.Rol rol
) {}