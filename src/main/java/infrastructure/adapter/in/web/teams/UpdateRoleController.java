package infrastructure.adapter.in.web.teams;

import domain.model.team.Role;
import domain.port.teams.in.UpdateRoleUseCase;
import infrastructure.adapter.in.web.teams.dto.ErrorResponse;
import infrastructure.adapter.in.web.teams.dto.RoleResponse;
import infrastructure.adapter.in.web.teams.dto.UpdateRoleRequest;
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
@RequestMapping("/api/teams/roles")
@Tag(name = "Team Roles", description = "Endpoints for team role management")
public class UpdateRoleController {

    private static final Logger log = LoggerFactory.getLogger(UpdateRoleController.class);

    private final UpdateRoleUseCase updateRoleUseCase;

    public UpdateRoleController(UpdateRoleUseCase updateRoleUseCase) {
        this.updateRoleUseCase = updateRoleUseCase;
    }

    @Operation(summary = "Update Role", description = "Update name or permissions of a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody UpdateRoleRequest request) {
        try {
            Role role = updateRoleUseCase.updateRole(id, request.name(), request.permissions());

            RoleResponse response = new RoleResponse(
                    role.getId(), role.getTeamId(), role.getName(),
                    role.getPermissions(), role.getIsDefault(),
                    role.getCreatedAt(), role.getUpdatedAt()
            );

            log.info("Role updated: id={}, name={}", response.id(), response.name());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update role: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage(), 400), HttpStatus.BAD_REQUEST);
        }
    }
}
