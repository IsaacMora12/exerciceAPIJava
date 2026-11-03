package infrastructure.adapter.in.web.exercise;

import domain.port.exercise.in.ViewMuscleUseCase;
import infrastructure.adapter.in.web.MediaUrlResolver;
import infrastructure.adapter.in.web.exercise.dto.MuscleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/muscle")
@Tag(name = "Muscle", description = "Endpoints for muscle management")
public class ViewAllMuscleController {

    private static final Logger log = LoggerFactory.getLogger(ViewAllMuscleController.class);

    private final ViewMuscleUseCase viewMuscleUseCase;
    private final MediaUrlResolver mediaUrlResolver;

    public ViewAllMuscleController(ViewMuscleUseCase viewMuscleUseCase,
                                   MediaUrlResolver mediaUrlResolver) {
        this.viewMuscleUseCase = viewMuscleUseCase;
        this.mediaUrlResolver = mediaUrlResolver;
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
    public ResponseEntity<List<MuscleResponse>> viewMuscle() {
        List<MuscleResponse> response = viewMuscleUseCase.findAll()
                .stream()
                .map(muscle -> new MuscleResponse(
                        muscle.getId(),
                        muscle.getName(),
                        muscle.getDescription(),
                        mediaUrlResolver.toUrls(muscle.getImages()),
                        muscle.getIsActive(),
                        muscle.getCreatedAt(),
                        muscle.getUpdatedAt(),
                        muscle.getUpdatedBy()
                ))
                .toList();
        if (response.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No muscles found");
        }

        log.info("Muscles retrieved successfully: count={}", response.size());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
