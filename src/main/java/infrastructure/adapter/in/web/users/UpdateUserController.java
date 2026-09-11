package infrastructure.adapter.in.web.users;

import domain.model.user.User;
import domain.port.users.in.UpdateUserUseCase;
import infrastructure.adapter.in.web.users.dto.UpdateUserRequest;
import infrastructure.adapter.in.web.users.dto.ErrorResponse;
import infrastructure.adapter.in.web.users.dto.UserResponse;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for the user management")
public class UpdateUserController {

    private static final Logger log = LoggerFactory.getLogger(UpdateUserController.class);

    private final UpdateUserUseCase updateUserUseCase;

    public UpdateUserController(UpdateUserUseCase updateUserUseCase) {
        this.updateUserUseCase = updateUserUseCase;
    }

    @Operation(
            summary = "Update User",
            description = "Update name, email, password or active state of an existing user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The user was successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @org.springframework.web.bind.annotation.RequestBody UpdateUserRequest request) {
        User updateUser = updateUserUseCase.updateUser(id, request.name(), request.email(), request.password(), request.isActive(), request.rol());

        UserResponse response = new UserResponse(
                updateUser.getId(),
                updateUser.getName(),
                updateUser.getEmail(),
                updateUser.getRol(),
                updateUser.getIsActive()
        );

        log.info("User updated successfully: id={}, email={}", response.id(), response.email());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}