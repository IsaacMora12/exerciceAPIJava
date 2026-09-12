package infrastructure.adapter.in.web.exercise;

import domain.model.exercice.Muscle;
import domain.port.exercise.in.CreateMuscleUseCase;
import domain.port.storage.ImageStorageService;
import infrastructure.adapter.in.web.exercise.dto.CreateMuscleRequest;
import infrastructure.adapter.in.web.exercise.dto.MuscleResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/muscle")
@Tag(name = "Muscle", description = "Endpoints for muscle management")
public class CreateMuscleController {

    private static final Logger log = LoggerFactory.getLogger(CreateMuscleController.class);

    private final CreateMuscleUseCase createMuscleUseCase;
    private final ImageStorageService imageStorageService;

    public CreateMuscleController(CreateMuscleUseCase createMuscleUseCase,
                                   ImageStorageService imageStorageService) {
        this.createMuscleUseCase = createMuscleUseCase;
        this.imageStorageService = imageStorageService;
    }

    @Operation(
            summary = "Create Muscle",
            description = "Create a new muscle with optional image upload. Send as multipart/form-data."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The muscle was successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(path = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createMuscle(
            @Parameter(
                    description = "Muscle data as JSON",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateMuscleRequest.class),
                            examples = @ExampleObject(
                                    name = "MuscleData",
                                    summary = "JSON payload for muscle data",
                                    value = """
                        {
                          "name": "Bicep",
                          "description": "Front upper arm muscle",
                          "images": []
                        }
                        """
                            )
                    )
            )
            @RequestPart("data") CreateMuscleRequest request,

            @Parameter(
                    description = "Image file (jpg, png, webp — max 5MB)",
                    required = false,
                    content = @Content(
                            mediaType = "image/jpeg, image/png, image/webp"
                    )
            )
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
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

            Muscle createdMuscle = createMuscleUseCase.createMuscle(
                    request.name(), request.description(), images, userId);

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
