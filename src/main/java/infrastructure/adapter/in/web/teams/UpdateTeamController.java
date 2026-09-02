package infrastructure.adapter.in.web.teams;

import domain.model.team.Team;
import domain.port.teams.in.UpdateTeamUseCase;
import infrastructure.adapter.in.web.teams.dto.ErrorResponse;
import infrastructure.adapter.in.web.teams.dto.TeamResponse;
import infrastructure.adapter.in.web.teams.dto.UpdateTeamRequest;
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
public class UpdateTeamController {

    private static final Logger log = LoggerFactory.getLogger(UpdateTeamController.class);

    private final UpdateTeamUseCase updateTeamUseCase;

    public UpdateTeamController(UpdateTeamUseCase updateTeamUseCase) {
        this.updateTeamUseCase = updateTeamUseCase;
    }

    @Operation(
            summary = "Update Team",
            description = "Update name, slug, description or owner of an existing team"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The team was successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeam(@PathVariable Long id, @RequestBody UpdateTeamRequest request) {
        try {
            Team updatedTeam = updateTeamUseCase.updateTeam(id, request.name(), request.slug(), request.description(), request.owner(), null);

            TeamResponse response = new TeamResponse(
                    updatedTeam.getId(),
                    updatedTeam.getName(),
                    updatedTeam.getSlug(),
                    updatedTeam.getDescription(),
                    updatedTeam.getOwner(),
                    updatedTeam.getIsActive(),
                    updatedTeam.getCreatedAt(),
                    updatedTeam.getUpdatedAt()
            );

            log.info("Team updated successfully: id={}, name={}", response.id(), response.name());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update team: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(e.getMessage(), 400);
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
