package infrastructure.adapter.in.web.exercise;

import domain.model.exercice.Exercise;
import domain.port.exercise.in.CreateExerciseUseCase;
import domain.port.storage.ImageStorageService;
import domain.port.storage.VideoStorageService;
import infrastructure.adapter.in.web.exercise.dto.CreateExerciseRequest;
import infrastructure.adapter.in.web.exercise.dto.ExerciseResponse;
import infrastructure.adapter.in.web.teams.dto.ErrorResponse;
import infrastructure.adapter.out.persistence.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/exercise")
@Tag(name = "Exercise", description = "Endpoints for exercise management")
public class CreateExerciseController {

    private static final Logger log = LoggerFactory.getLogger(CreateExerciseController.class);

    private final CreateExerciseUseCase createExerciseUseCase;
    private final ImageStorageService imageStorageService;
    private final VideoStorageService videoStorageService;

    public CreateExerciseController(CreateExerciseUseCase createExerciseUseCase,
                                     ImageStorageService imageStorageService,
                                     VideoStorageService videoStorageService) {
        this.createExerciseUseCase = createExerciseUseCase;
        this.imageStorageService = imageStorageService;
        this.videoStorageService = videoStorageService;
    }

    @Operation(
            summary = "Create Exercise",
            description = "Create a new exercise with optional image/video upload. Send as multipart/form-data."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The exercise was successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(path = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createExercise(
            @Parameter(
                    description = "Exercise data as JSON",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateExerciseRequest.class),
                            examples = @ExampleObject(
                                    name = "ExerciseData",
                                    summary = "JSON payload for exercise data",
                                    value = """
                        {
                          "name": "Barbell Bicep Curl",
                          "description": "Standing curl with barbell targeting biceps",
                          "mainMuscle": 1,
                          "othersMuscle": [2, 3],
                          "images": [],
                          "videos": []
                        }
                        """
                            )
                    )
            )
            @RequestPart("data") CreateExerciseRequest request,

            @Parameter(
                    description = "Image file (jpg, png, webp — max 5MB)",
                    required = false
            )
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,

            @Parameter(
                    description = "Video/GIF file (gif, mp4, webm, mov — max 50MB)",
                    required = false
            )
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            Long userId = currentUser.getId();

            // Imágenes del JSON + imagen subida via multipart
            List<String> images = new ArrayList<>(
                    request.images() != null ? request.images() : List.of());

            if (imageFile != null && !imageFile.isEmpty()) {
                String imageKey = imageStorageService.upload(imageFile);
                images.add(imageKey);
            }

            // Videos del JSON + GIF/archivo subido via multipart
            List<String> videos = new ArrayList<>(
                    request.videos() != null ? request.videos() : List.of());

            if (videoFile != null && !videoFile.isEmpty()) {
                String videoKey = videoStorageService.upload(videoFile);
                videos.add(videoKey);
            }

            Exercise createdExercise = createExerciseUseCase.createExercise(
                    request.name(), request.description(), request.category(),
                    request.instruccion(), request.equipament(),
                    request.mainMuscle(), request.othersMuscle(),
                    images, videos, userId);

            ExerciseResponse response = new ExerciseResponse(
                    createdExercise.getId(),
                    createdExercise.getName(),
                    createdExercise.getDescription(),
                    createdExercise.getCategory(),
                    createdExercise.getInstruccion(),
                    createdExercise.getEquipament(),
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
