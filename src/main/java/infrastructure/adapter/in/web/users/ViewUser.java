package infrastructure.adapter.in.web.users;

import domain.port.users.in.ViewUserUseCase;
import infrastructure.adapter.in.web.users.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for the user management")
public class ViewUser {

    private static final Logger log = LoggerFactory.getLogger(ViewUser.class);

    private final ViewUserUseCase viewUserUseCase;

    public ViewUser(ViewUserUseCase viewUserUseCase) {
        this.viewUserUseCase = viewUserUseCase;
    }
    @Operation(
            summary = "View User ",
            description = "Retrieve a user by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The user was successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> viewUser(@PathVariable("id") Long id) {
        UserResponse response = viewUserUseCase.viewUser(id)
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getIsActive()
                ))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        log.info("User retrieved successfully: id={}, email={}", response.id(), response.email());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}