package infrastructure.adapter.in.web.exercise;

import domain.port.exercise.in.ViewExerciseUseCase;
import infrastructure.adapter.in.web.MediaUrlResolver;
import infrastructure.adapter.in.web.exercise.dto.ExerciseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/exercise")
@Tag(name = "Exercise", description = "Endpoints for exercise management")
public class ViewExerciseController {

    private static final Logger log = LoggerFactory.getLogger(ViewExerciseController.class);

    private final ViewExerciseUseCase viewExerciseUseCase;
    private final MediaUrlResolver mediaUrlResolver;

    public ViewExerciseController(ViewExerciseUseCase viewExerciseUseCase,
                                  MediaUrlResolver mediaUrlResolver) {
        this.viewExerciseUseCase = viewExerciseUseCase;
        this.mediaUrlResolver = mediaUrlResolver;
    }

    @Operation(
            summary = "View Exercise",
            description = "Retrieve an exercise by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The exercise was successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Exercise not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> viewExercise(@PathVariable("id") Long id) {
        ExerciseResponse response = viewExerciseUseCase.viewExercise(id)
                .map(exercise -> new ExerciseResponse(
                        exercise.getId(),
                        exercise.getName(),
                        exercise.getDescription(),
                        exercise.getCategory(),
                        exercise.getInstruccion(),
                        exercise.getEquipament(),
                        exercise.getMainMuscle(),
                        exercise.getOthersMuscle(),
                        mediaUrlResolver.toUrls(exercise.getImages()),
                        mediaUrlResolver.toUrls(exercise.getVideos()),
                        exercise.getIsActive(),
                        exercise.getCreatedAt(),
                        exercise.getUpdatedAt(),
                        exercise.getUpdatedBy()
                ))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));
        log.info("Exercise retrieved successfully: id={}, name={}", response.id(), response.name());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
