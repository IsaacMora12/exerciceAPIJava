package infrastructure.adapter.in.web.teams;

import domain.port.teams.in.DeleteTeamUseCase;
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
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Endpoints for team management")
public class DeleteTeamController {

    private static final Logger log = LoggerFactory.getLogger(DeleteTeamController.class);

    private final DeleteTeamUseCase deleteTeamUseCase;

    public DeleteTeamController(DeleteTeamUseCase deleteTeamUseCase) {
        this.deleteTeamUseCase = deleteTeamUseCase;
    }

    @Operation(
            summary = "Delete Team",
            description = "Delete a team by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The team was successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Team not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long id) {
        try {
            deleteTeamUseCase.deleteTeam(id);
            log.info("Team deleted successfully: id={}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete team: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(e.getMessage(), 404);
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }
}
