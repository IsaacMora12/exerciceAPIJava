package infrastructure.adapter.in.web.exercise;

import domain.port.exercise.in.DeleteExerciseUseCase;
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
@RequestMapping("/api/exercise")
@Tag(name = "Exercise", description = "Endpoints for exercise management")
public class DeleteExerciseController {

    private static final Logger log = LoggerFactory.getLogger(DeleteExerciseController.class);

    private final DeleteExerciseUseCase deleteExerciseUseCase;

    public DeleteExerciseController(DeleteExerciseUseCase deleteExerciseUseCase) {
        this.deleteExerciseUseCase = deleteExerciseUseCase;
    }

    @Operation(
            summary = "Delete Exercise",
            description = "Delete an exercise by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The exercise was successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Exercise not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExercise(@PathVariable Long id) {
        try {
            deleteExerciseUseCase.deleteExercise(id);
            log.info("Exercise deleted successfully: id={}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete exercise: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(e.getMessage(), 404);
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }
}
