package infrastructure.adapter.in.web.teams;

import domain.port.teams.in.DeleteRoleUseCase;
import infrastructure.adapter.in.web.teams.dto.ErrorResponse;
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
public class DeleteRoleController {

    private static final Logger log = LoggerFactory.getLogger(DeleteRoleController.class);

    private final DeleteRoleUseCase deleteRoleUseCase;

    public DeleteRoleController(DeleteRoleUseCase deleteRoleUseCase) {
        this.deleteRoleUseCase = deleteRoleUseCase;
    }

    @Operation(summary = "Delete Role", description = "Delete a role by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        try {
            deleteRoleUseCase.deleteRole(id);
            log.info("Role deleted: id={}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete role: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage(), 404), HttpStatus.NOT_FOUND);
        }
    }
}
