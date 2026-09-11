package domain.model.exercice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Muscle
{
    private Long id;
    private String name;
    private String description;
    private List<String> images;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    private Muscle(Long id, String name, String description, List<String> images,
                   Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt,
                   Long updatedBy )
    {
        validateName(name);
        this.id = id;
        this.name = name;
        this.description = description;
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isActive = isActive;
        this.updatedBy = updatedBy;
    }

    public static Muscle create(String name, String description, List<String> images , Long updatedBy) {
        return new Muscle(null, name, description, images,
                true, LocalDateTime.now(), LocalDateTime.now(), updatedBy);
    }
    public static Muscle reconstruct(Long id, String name, String description, List<String> images,
                                 Boolean isActive,
                                   LocalDateTime createdAt, LocalDateTime updatedAt, Long updatedBy) {
        if (id == null) {
            throw new IllegalArgumentException("The id must not be empty for an existing user");
        }
        return new Muscle(id, name, description, images, isActive, createdAt, updatedAt, updatedBy);
    }

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
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("The name must not be empty");
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



    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getImages() { return Collections.unmodifiableList(images); }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getUpdatedBy() { return updatedBy; }

    public void updateUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
        updateTimestamp();
    }
}
