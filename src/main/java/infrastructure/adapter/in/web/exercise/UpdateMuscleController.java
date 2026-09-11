package infrastructure.adapter.in.web.exercise;

import domain.model.exercice.Muscle;
import domain.port.exercise.in.UpdateMuscleUseCase;
import infrastructure.adapter.in.web.exercise.dto.MuscleResponse;
import infrastructure.adapter.in.web.exercise.dto.UpdateMuscleRequest;
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
@RequestMapping("/api/muscle")
@Tag(name = "Muscle", description = "Endpoints for muscle management")
public class UpdateMuscleController {

    private static final Logger log = LoggerFactory.getLogger(UpdateMuscleController.class);

    private final UpdateMuscleUseCase updateMuscleUseCase;

    public UpdateMuscleController(UpdateMuscleUseCase updateMuscleUseCase) {
        this.updateMuscleUseCase = updateMuscleUseCase;
    }

    @Operation(
            summary = "Update Muscle",
            description = "Update a muscle. The updatedBy field comes from the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The muscle was successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Muscle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMuscle(@PathVariable Long id, @RequestBody UpdateMuscleRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            Long userId = currentUser.getId();

            Muscle updatedMuscle = updateMuscleUseCase.updateMuscle(
                    id, request.name(), request.description(), request.images(),
                    request.isActive(), userId);

            MuscleResponse response = new MuscleResponse(
                    updatedMuscle.getId(),
                    updatedMuscle.getName(),
                    updatedMuscle.getDescription(),
                    updatedMuscle.getImages(),
                    updatedMuscle.getIsActive(),
                    updatedMuscle.getCreatedAt(),
                    updatedMuscle.getUpdatedAt(),
                    updatedMuscle.getUpdatedBy()
            );

            log.info("Muscle updated successfully: id={}, name={}, updatedBy={}", response.id(), response.name(), userId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update muscle: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(e.getMessage(), 400);
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
