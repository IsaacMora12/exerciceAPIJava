package infrastructure.adapter.in.web.exercise;

import domain.port.exercise.in.ViewMuscleUseCase;
import infrastructure.adapter.in.web.exercise.dto.MuscleResponse;
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
@RequestMapping("/api/muscle")
@Tag(name = "Muscle", description = "Endpoints for muscle management")
public class ViewMuscleController {

    private static final Logger log = LoggerFactory.getLogger(ViewMuscleController.class);

    private final ViewMuscleUseCase viewMuscleUseCase;

    public ViewMuscleController(ViewMuscleUseCase viewMuscleUseCase) {
        this.viewMuscleUseCase = viewMuscleUseCase;
    }

    @Operation(
            summary = "View Muscle",
            description = "Retrieve a muscle by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The muscle was successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Muscle not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MuscleResponse> viewMuscle(@PathVariable("id") Long id) {
        MuscleResponse response = viewMuscleUseCase.viewMuscle(id)
                .map(muscle -> new MuscleResponse(
                        muscle.getId(),
                        muscle.getName(),
                        muscle.getDescription(),
                        muscle.getImages(),
                        muscle.getIsActive(),
                        muscle.getCreatedAt(),
                        muscle.getUpdatedAt(),
                        muscle.getUpdatedBy()
                ))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Muscle not found"));
        log.info("Muscle retrieved successfully: id={}, name={}", response.id(), response.name());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
