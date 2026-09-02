package infrastructure.adapter.in.web.teams;

import domain.model.team.Role;
import domain.port.teams.in.CreateRoleUseCase;
import infrastructure.adapter.in.web.teams.dto.CreateRoleRequest;
import infrastructure.adapter.in.web.teams.dto.ErrorResponse;
import infrastructure.adapter.in.web.teams.dto.RoleResponse;
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
@RequestMapping("/api/teams/{teamId}/roles")
@Tag(name = "Team Roles", description = "Endpoints for team role management")
public class CreateRoleController {

    private static final Logger log = LoggerFactory.getLogger(CreateRoleController.class);

    private final CreateRoleUseCase createRoleUseCase;

    public CreateRoleController(CreateRoleUseCase createRoleUseCase) {
        this.createRoleUseCase = createRoleUseCase;
    }

    @Operation(summary = "Create Role", description = "Create a new role in a team")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> createRole(@PathVariable Long teamId, @RequestBody CreateRoleRequest request) {
        try {
            Role role = createRoleUseCase.createRole(teamId, request.name(), request.permissions(), request.isDefault());

            RoleResponse response = new RoleResponse(
                    role.getId(), role.getTeamId(), role.getName(),
                    role.getPermissions(), role.getIsDefault(),
                    role.getCreatedAt(), role.getUpdatedAt()
            );

            log.info("Role created: id={}, name={}", response.id(), response.name());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create role: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage(), 400), HttpStatus.BAD_REQUEST);
        }
    }
}
