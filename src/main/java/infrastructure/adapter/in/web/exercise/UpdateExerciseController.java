package infrastructure.adapter.in.web.exercise;

import domain.model.exercice.Exercise;
import domain.port.exercise.in.UpdateExerciseUseCase;
import infrastructure.adapter.in.web.exercise.dto.ExerciseResponse;
import infrastructure.adapter.in.web.exercise.dto.UpdateExerciseRequest;
import infrastructure.adapter.in.web.teams.dto.ErrorResponse;
import infrastructure.adapter.out.persistence.auth.CustomUserDetails;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exercise")
@Tag(name = "Exercise", description = "Endpoints for exercise management")
public class UpdateExerciseController {

    private static final Logger log = LoggerFactory.getLogger(UpdateExerciseController.class);

    private final UpdateExerciseUseCase updateExerciseUseCase;

    public UpdateExerciseController(UpdateExerciseUseCase updateExerciseUseCase) {
        this.updateExerciseUseCase = updateExerciseUseCase;
    }

    @Operation(
            summary = "Update Exercise",
            description = "Update an exercise. The updatedBy field comes from the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The exercise was successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exercise not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExercise(@PathVariable Long id, @RequestBody UpdateExerciseRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            Long userId = currentUser.getId();

            Exercise updatedExercise = updateExerciseUseCase.updateExercise(
                    id, request.name(), request.description(), request.category(),
                    request.instruccion(), request.equipament(),
                    request.mainMuscle(), request.othersMuscle(),
                    request.images(), request.videos(),
                    request.isActive(), userId);

            ExerciseResponse response = new ExerciseResponse(
                    updatedExercise.getId(),
                    updatedExercise.getName(),
                    updatedExercise.getDescription(),
                    updatedExercise.getCategory(),
                    updatedExercise.getInstruccion(),
                    updatedExercise.getEquipament(),
                    updatedExercise.getMainMuscle(),
                    updatedExercise.getOthersMuscle(),
                    updatedExercise.getImages(),
                    updatedExercise.getVideos(),
                    updatedExercise.getIsActive(),
                    updatedExercise.getCreatedAt(),
                    updatedExercise.getUpdatedAt(),
                    updatedExercise.getUpdatedBy()
            );

            log.info("Exercise updated successfully: id={}, name={}, updatedBy={}", response.id(), response.name(), userId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update exercise: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(e.getMessage(), 400);
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
