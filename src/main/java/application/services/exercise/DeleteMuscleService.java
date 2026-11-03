package application.services.exercise;

import domain.model.exercise.Muscle;
import domain.port.exercise.MuscleRepository;
import domain.port.exercise.in.DeleteMuscleUseCase;
import domain.port.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeleteMuscleService implements DeleteMuscleUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteMuscleService.class);

    private final MuscleRepository muscleRepository;
    private final FileStorageService fileStorageService;

    public DeleteMuscleService(MuscleRepository muscleRepository,
                               FileStorageService fileStorageService) {
        this.muscleRepository = muscleRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void deleteMuscle(Long id) {
        Muscle muscle = muscleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Muscle not found"));

        if (Boolean.TRUE.equals(muscleRepository.isMuscleUsed(id))) {
            throw new IllegalArgumentException("This muscle is in use and cannot be deleted");
        }

        deleteStoredFiles(muscle.getImages());

        muscleRepository.deleteById(id);
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
