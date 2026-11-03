package application.services.exercise;

import domain.model.exercise.Exercise;
import domain.port.exercise.ExerciseRepository;
import domain.port.exercise.in.DeleteExerciseUseCase;
import domain.port.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeleteExerciseService implements DeleteExerciseUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteExerciseService.class);

    private final ExerciseRepository exerciseRepository;
    private final FileStorageService fileStorageService;

    public DeleteExerciseService(ExerciseRepository exerciseRepository,
                                 FileStorageService fileStorageService) {
        this.exerciseRepository = exerciseRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void deleteExercise(Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found with id: " + id));

        deleteStoredFiles(exercise.getImages());
        deleteStoredFiles(exercise.getVideos());

        exerciseRepository.deleteById(id);
    }

    /**
     * Best-effort: un fallo al borrar un objeto NO debe impedir el borrado de la
     * entity — se loguea warning y se sigue. Se ignoran valores con "://"
     * ( URLs legacy / dobles de test ), que no son claves de storage
     * (misma regla que {@code MediaUrlResolver}).
     */
    private void deleteStoredFiles(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            return;
        }
        for (String fileKey : fileKeys) {
            if (fileKey == null || fileKey.isBlank() || fileKey.contains("://")) {
                continue;
            }
            try {
                fileStorageService.delete(fileKey);
            } catch (Exception e) {
                log.warn("Failed to delete stored file '{}' (continuing with entity delete): {}",
                        fileKey, e.getMessage());
            }
        }
    }
}
