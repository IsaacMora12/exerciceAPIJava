package infrastructure.adapter.in.web.exercise;

import domain.port.exercise.in.DeleteMuscleUseCase;
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
@RequestMapping("/api/muscle")
@Tag(name = "Muscle", description = "Endpoints for muscle management")
public class DeleteMuscleController {

    private static final Logger log = LoggerFactory.getLogger(DeleteMuscleController.class);

    private final DeleteMuscleUseCase deleteMuscleUseCase;

    public DeleteMuscleController(DeleteMuscleUseCase deleteMuscleUseCase) {
        this.deleteMuscleUseCase = deleteMuscleUseCase;
    }

    @Operation(
            summary = "Delete Muscle",
            description = "Delete a muscle by ID. Fails if the muscle is in use by any exercise."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The muscle was successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Muscle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Muscle is in use and cannot be deleted",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMuscle(@PathVariable Long id) {
        try {
            deleteMuscleUseCase.deleteMuscle(id);
            log.info("Muscle deleted successfully: id={}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete muscle: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(e.getMessage(), 400);
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
