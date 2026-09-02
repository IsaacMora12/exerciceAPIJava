package infrastructure.adapter.in.web.teams;

import domain.port.teams.in.ViewTeamUseCase;
import infrastructure.adapter.in.web.teams.dto.TeamResponse;
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
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Endpoints for team management")
public class ViewTeamController {

    private static final Logger log = LoggerFactory.getLogger(ViewTeamController.class);

    private final ViewTeamUseCase viewTeamUseCase;

    public ViewTeamController(ViewTeamUseCase viewTeamUseCase) {
        this.viewTeamUseCase = viewTeamUseCase;
    }

    @Operation(
            summary = "View Team",
            description = "Retrieve a team by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The team was successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Team not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> viewTeam(@PathVariable("id") Long id) {
        TeamResponse response = viewTeamUseCase.viewTeam(id)
                .map(team -> new TeamResponse(
                        team.getId(),
                        team.getName(),
                        team.getSlug(),
                        team.getDescription(),
                        team.getOwner(),
                        team.getIsActive(),
                        team.getCreatedAt(),
                        team.getUpdatedAt()
                ))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        log.info("Team retrieved successfully: id={}, name={}", response.id(), response.name());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
