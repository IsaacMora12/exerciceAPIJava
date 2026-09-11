package domain.model.exercice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Exercise {

    private Long id;
    private String name;
    private String description;
    private Long mainMuscle;
    private List<Long> othersMuscle;
    private List<String> images;
    private List<String> videos;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    private Exercise(Long id, String name, String description, Long mainMuscle,
                     List<Long> othersMuscle, List<String> images, List<String> videos,
                     Boolean isActive,
                     LocalDateTime createdAt, LocalDateTime updatedAt, Long updatedBy) {
        validateName(name);
        validateMainMuscle(mainMuscle);
        this.id = id;
        this.name = name;
        this.description = description;
        this.mainMuscle = mainMuscle;
        this.othersMuscle = othersMuscle != null ? new ArrayList<>(othersMuscle) : new ArrayList<>();
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.videos = videos != null ? new ArrayList<>(videos) : new ArrayList<>();
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    // Factory 1: Crear ejercicio nuevo
    public static Exercise create(String name, String description, Long mainMuscle,
                                  List<Long> othersMuscle, List<String> images,
                                  List<String> videos, Long updatedBy) {
        LocalDateTime now = LocalDateTime.now();
        return new Exercise(null, name, description, mainMuscle,
                othersMuscle, images, videos, true, now, now, updatedBy);
    }

    // Factory 2: Reconstruir ejercicio desde la BD
    public static Exercise reconstruct(Long id, String name, String description, Long mainMuscle,
                                       List<Long> othersMuscle, List<String> images,
                                       List<String> videos, Boolean isActive,
                                       LocalDateTime createdAt, LocalDateTime updatedAt, Long updatedBy) {
        if (id == null) {
            throw new IllegalArgumentException("The id must not be empty for an existing exercise");
        }
        return new Exercise(id, name, description, mainMuscle,
                othersMuscle, images, videos, isActive, createdAt, updatedAt, updatedBy);
    }

    // Métodos de negocio
    public void activate() {
        this.isActive = true;
        updateTimestamp();
    }

    public void deactivate() {
        this.isActive = false;
        updateTimestamp();
    }

    public void updateName(String name) {
        validateName(name);
        this.name = name;
        updateTimestamp();
    }

    public void updateDescription(String description) {
        this.description = description;
        updateTimestamp();
    }

    public void updateMainMuscle(Long mainMuscle) {
        validateMainMuscle(mainMuscle);
        this.mainMuscle = mainMuscle;
        updateTimestamp();
    }

    public void updateOthersMuscle(List<Long> othersMuscle) {
        this.othersMuscle = othersMuscle != null ? new ArrayList<>(othersMuscle) : new ArrayList<>();
        updateTimestamp();
    }

    public void addOtherMuscle(Long muscleId) {
        if (muscleId == null) {
            throw new IllegalArgumentException("The muscle id must not be null");
        }
        if (this.othersMuscle.contains(muscleId)) {
            throw new IllegalArgumentException("The muscle with id " + muscleId + " is already added");
        }
        this.othersMuscle.add(muscleId);
        updateTimestamp();
    }

    public void removeOtherMuscle(Long muscleId) {
        if (this.othersMuscle.remove(muscleId)) {
            updateTimestamp();
        }
    }

    // Métodos de negocio — images
    public void updateImages(List<String> images) {
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        updateTimestamp();
    }

    public void addImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("The image URL must not be empty");
        }
        if (!this.images.contains(imageUrl)) {
            this.images.add(imageUrl);
            updateTimestamp();
        }
    }

    public void removeImage(String imageUrl) {
        if (this.images.remove(imageUrl)) {
            updateTimestamp();
        }
    }

    // Métodos de negocio — videos
    public void updateVideos(List<String> videos) {
        this.videos = videos != null ? new ArrayList<>(videos) : new ArrayList<>();
        updateTimestamp();
    }

    public void addVideo(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("The video URL must not be empty");
        }
        if (!this.videos.contains(videoUrl)) {
            this.videos.add(videoUrl);
            updateTimestamp();
        }
    }

    public void removeVideo(String videoUrl) {
        if (this.videos.remove(videoUrl)) {
            updateTimestamp();
        }
    }

    public boolean hasImages() {
        return this.images != null && !this.images.isEmpty();
    }

    public boolean hasVideos() {
        return this.videos != null && !this.videos.isEmpty();
    }

    public boolean hasMainMuscle() {
        return this.mainMuscle != null;
    }

    public boolean hasOtherMuscles() {
        return this.othersMuscle != null && !this.othersMuscle.isEmpty();
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // Validaciones del dominio
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("The name must not be empty");
        }
    }

    private void validateMainMuscle(Long mainMuscle) {
        if (mainMuscle == null) {
            throw new IllegalArgumentException("The main muscle must not be empty");
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getMainMuscle() { return mainMuscle; }
    public List<Long> getOthersMuscle() { return Collections.unmodifiableList(othersMuscle); }
    public List<String> getImages() { return Collections.unmodifiableList(images); }
    public List<String> getVideos() { return Collections.unmodifiableList(videos); }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getUpdatedBy() { return updatedBy; }

    public void updateUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
        updateTimestamp();
    }
}
