package infrastructure.adapter.in.web.teams;

import domain.model.team.Team;
import domain.port.teams.in.CreateTeamUseCase;
import infrastructure.adapter.in.web.teams.dto.CreateTeamRequest;
import infrastructure.adapter.in.web.teams.dto.ErrorResponse;
import infrastructure.adapter.in.web.teams.dto.TeamResponse;
import infrastructure.adapter.out.persistence.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Endpoints for team management")
public class CreateTeamController {

    private static final Logger log = LoggerFactory.getLogger(CreateTeamController.class);

    private final CreateTeamUseCase createTeamUseCase;

    public CreateTeamController(CreateTeamUseCase createTeamUseCase) {
        this.createTeamUseCase = createTeamUseCase;
    }

    @Operation(
            summary = "Create Team",
            description = "Create a new team. The owner is the authenticated user from the JWT token."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload required to create a new team (owner comes from JWT token)",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateTeamRequest.class),
                    examples = @ExampleObject(
                            name = "StandardTeamExample",
                            summary = "Example payload for creating a team",
                            value = """
                {
                  "name": "Engineering",
                  "slug": "engineering",
                  "description": "Backend development team"
                }
                """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The team was successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<?> createTeam(@RequestBody CreateTeamRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            Long ownerId = currentUser.getId();

            Team createdTeam = createTeamUseCase.createTeam(
                    request.name(), request.slug(), request.description(), ownerId);

            TeamResponse response = new TeamResponse(
                    createdTeam.getId(),
                    createdTeam.getName(),
                    createdTeam.getSlug(),
                    createdTeam.getDescription(),
                    createdTeam.getOwner(),
                    createdTeam.getIsActive(),
                    createdTeam.getCreatedAt(),
                    createdTeam.getUpdatedAt()
            );

            log.info("Team created successfully: id={}, name={}, owner={}", response.id(), response.name(), ownerId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create team: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(e.getMessage(), 400);
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
