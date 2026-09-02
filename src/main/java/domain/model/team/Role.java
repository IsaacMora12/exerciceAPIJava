package domain.model.team;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Role {

    private Long id;
    private Long teamId;
    private String name;
    private Map<String, Boolean> permissions;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Role(Long id, Long teamId, String name, Map<String, Boolean> permissions,
                 Boolean isDefault, LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateName(name);
        this.id = id;
        this.teamId = teamId;
        this.name = name;
        this.permissions = permissions != null ? permissions : new HashMap<>();
        this.isDefault = isDefault != null ? isDefault : false;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Factory 1: Create new role
    public static Role create(Long teamId, String name, Map<String, Boolean> permissions, Boolean isDefault) {
        return new Role(null, teamId, name, permissions, isDefault, LocalDateTime.now(), LocalDateTime.now());
    }

    // Factory 2: Reconstruct from DB
    public static Role reconstruct(Long id, Long teamId, String name, Map<String, Boolean> permissions,
                                   Boolean isDefault, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("The id must not be empty for an existing role");
        }
        return new Role(id, teamId, name, permissions, isDefault, createdAt, updatedAt);
    }

    public void updateName(String name) {
        validateName(name);
        this.name = name;
        updateTimestamp();
    }

    public void updatePermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions != null ? permissions : new HashMap<>();
        updateTimestamp();
    }

    public boolean hasPermission(String permission) {
        return Boolean.TRUE.equals(this.permissions.get(permission));
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("The role name must not be empty");
        }
    }

    // Getters
    public Long getId() { return id; }
    public Long getTeamId() { return teamId; }
    public String getName() { return name; }
    public Map<String, Boolean> getPermissions() { return permissions; }
    public Boolean getIsDefault() { return isDefault; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
