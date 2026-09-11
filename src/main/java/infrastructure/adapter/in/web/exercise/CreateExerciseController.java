package infrastructure.adapter.in.web.exercise;

import domain.model.exercice.Exercise;
import domain.port.exercise.in.CreateExerciseUseCase;
import infrastructure.adapter.in.web.exercise.dto.CreateExerciseRequest;
import infrastructure.adapter.in.web.exercise.dto.ExerciseResponse;
import infrastructure.adapter.in.web.teams.dto.ErrorResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exercise")
@Tag(name = "Exercise", description = "Endpoints for exercise management")
public class CreateExerciseController {

    private static final Logger log = LoggerFactory.getLogger(CreateExerciseController.class);

    private final CreateExerciseUseCase createExerciseUseCase;

    public CreateExerciseController(CreateExerciseUseCase createExerciseUseCase) {
        this.createExerciseUseCase = createExerciseUseCase;
    }

    @Operation(
            summary = "Create Exercise",
            description = "Create a new exercise. The updatedBy field comes from the authenticated user."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload required to create a new exercise (updatedBy comes from JWT token)",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateExerciseRequest.class),
                    examples = @ExampleObject(
                            name = "StandardExerciseExample",
                            summary = "Example payload for creating an exercise",
                            value = """
                {
                  "name": "Barbell Bicep Curl",
                  "description": "Standing curl with barbell targeting biceps",
                  "mainMuscle": 1,
                  "othersMuscle": [2, 3],
                  "images": ["https://example.com/bicep-curl.jpg"],
                  "videos": ["https://example.com/bicep-curl.mp4"]
                }
                """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The exercise was successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<?> createExercise(@RequestBody CreateExerciseRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            Long userId = currentUser.getId();

            Exercise createdExercise = createExerciseUseCase.createExercise(
                    request.name(), request.description(), request.mainMuscle(),
                    request.othersMuscle(), request.images(), request.videos(), userId);

            ExerciseResponse response = new ExerciseResponse(
                    createdExercise.getId(),
                    createdExercise.getName(),
                    createdExercise.getDescription(),
                    createdExercise.getMainMuscle(),
                    createdExercise.getOthersMuscle(),
                    createdExercise.getImages(),
                    createdExercise.getVideos(),
                    createdExercise.getIsActive(),
                    createdExercise.getCreatedAt(),
                    createdExercise.getUpdatedAt(),
                    createdExercise.getUpdatedBy()
            );

            log.info("Exercise created successfully: id={}, name={}, updatedBy={}", response.id(), response.name(), userId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create exercise: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(e.getMessage(), 400);
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
