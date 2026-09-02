package domain.model.team;

import java.time.LocalDateTime;

public class Team
{
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long owner;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Team(Long id, String name, String slug, String description, Long owner, Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt)
    {
        validateSlug(slug);
        validateOwner(owner);
        validateName(name);
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.slug = slug;
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    // Fábrica 1: Crear team nuevo
    public static Team create(String name, String slug, Long owner, String description) {
        return new Team(null, name, slug, description, owner, true, LocalDateTime.now(), LocalDateTime.now());
    }

    // Fábrica 2: Reconstruir team desde la BD
    public static Team reconstruct(Long id, String name, String slug, String description, Long owner, Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("The id must not be empty for an existing team");
        }
        return new Team(id, name, slug, description, owner, isActive, createdAt, updatedAt);
    }

    public void updateName(String name) {
        validateName(name);
        this.name = name;
        updateTimestamp();
    }
    public void updateSlug(String slug) {
        validateSlug(slug);
        this.slug = slug;
        updateTimestamp();
    }
    public void updateDescription(String description) {
        this.description = description;
        updateTimestamp();
    }
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    private void validateSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("The slug must not be empty");
        }
    }
    private void validateOwner(Long owner) {
        if (owner == null) {
            throw new IllegalArgumentException("The owner must not be empty");
        }
    }
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("The name must not be empty");
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public Long getOwner() { return owner; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
