package infrastructure.adapter.in.web.exercise;

import domain.model.exercice.Muscle;
import domain.port.exercise.in.CreateMuscleUseCase;
import infrastructure.adapter.in.web.exercise.dto.CreateMuscleRequest;
import infrastructure.adapter.in.web.exercise.dto.MuscleResponse;
import infrastructure.adapter.in.web.teams.dto.ErrorResponse;
import infrastructure.adapter.out.persistence.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/muscle")
@Tag(name = "Muscle", description = "Endpoints for muscle management")
public class CreateMuscleController {

    private static final Logger log = LoggerFactory.getLogger(CreateMuscleController.class);

    private final CreateMuscleUseCase createMuscleUseCase;

    public CreateMuscleController(CreateMuscleUseCase createMuscleUseCase) {
        this.createMuscleUseCase = createMuscleUseCase;
    }

    @Operation(
            summary = "Create Muscle",
            description = "Create a new muscle. The updatedBy field comes from the authenticated user."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload required to create a new muscle (updatedBy comes from JWT token)",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateMuscleRequest.class),
                    examples = @ExampleObject(
                            name = "StandardMuscleExample",
                            summary = "Example payload for creating a muscle",
                            value = """
                {
                  "name": "Bicep",
                  "description": "Front upper arm muscle",
                  "images": ["https://example.com/bicep.jpg"]
                }
                """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The muscle was successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<?> createMuscle(@RequestBody CreateMuscleRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            Long userId = currentUser.getId();

            Muscle createdMuscle = createMuscleUseCase.createMuscle(
                    request.name(), request.description(), request.images(), userId);

            MuscleResponse response = new MuscleResponse(
                    createdMuscle.getId(),
                    createdMuscle.getName(),
                    createdMuscle.getDescription(),
                    createdMuscle.getImages(),
                    createdMuscle.getIsActive(),
                    createdMuscle.getCreatedAt(),
                    createdMuscle.getUpdatedAt(),
                    createdMuscle.getUpdatedBy()
            );

            log.info("Muscle created successfully: id={}, name={}, updatedBy={}", response.id(), response.name(), userId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create muscle: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(e.getMessage(), 400);
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
