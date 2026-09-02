package infrastructure.adapter.in.web.users;

import domain.port.users.in.CreateUserUseCase;
import infrastructure.adapter.in.web.users.dto.CreateUserRequest;
import infrastructure.adapter.in.web.users.dto.ErrorResponse;
import infrastructure.adapter.in.web.users.dto.UserResponse;
import domain.model.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/create")
@Tag(name = "Users", description = "Endpoints for the user management")
public class CreateUserController {

    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);

    private final CreateUserUseCase createUserUseCase;

    public CreateUserController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }

    @Operation(
            summary = "Create User",
            description = "Create a new user in the application"
    )
    @RequestBody(
            description = "Payload required to create a new user",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateUserRequest.class),
                    examples = @ExampleObject(
                            name = "StandardUserExample",
                            summary = "Example payload for creating a regular user",
                            value = """
                {
                  "name": "John Doe",
                  "email": "john.doe@example.com",
                  "password": "SecurePassword123!"
                }
                """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The user was successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            User createdUser = createUserUseCase.createUser(request.name(), request.email(), request.password());

            UserResponse response = new UserResponse(
                    createdUser.getId(),
                    createdUser.getName(),
                    createdUser.getEmail(),
                    createdUser.getIsActive()
            );

            log.info("User created successfully: id={}, email={}", response.id(), response.email());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create user: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(e.getMessage(), 400);
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}