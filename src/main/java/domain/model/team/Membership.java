package domain.model.team;

import java.time.LocalDateTime;

public class Membership {

    private Long id;
    private Long userId;
    private Long teamId;
    private Long roleId;
    private Boolean isActive;
    private LocalDateTime joinedAt;
    private LocalDateTime updatedAt;

    private Membership(Long id, Long userId, Long teamId, Long roleId,
                       Boolean isActive, LocalDateTime joinedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.teamId = teamId;
        this.roleId = roleId;
        this.isActive = isActive != null ? isActive : true;
        this.joinedAt = joinedAt;
        this.updatedAt = updatedAt;
    }

    // Factory 1: Create new membership
    public static Membership create(Long userId, Long teamId, Long roleId) {
        LocalDateTime now = LocalDateTime.now();
        return new Membership(null, userId, teamId, roleId, true, now, now);
    }

    // Factory 2: Reconstruct from DB
    public static Membership reconstruct(Long id, Long userId, Long teamId, Long roleId,
                                         Boolean isActive, LocalDateTime joinedAt, LocalDateTime updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("The id must not be empty for an existing membership");
        }
        return new Membership(id, userId, teamId, roleId, isActive, joinedAt, updatedAt);
    }

    public void activate() {
        this.isActive = true;
        updateTimestamp();
    }

    public void deactivate() {
        this.isActive = false;
        updateTimestamp();
    }

    public void updateRoleId(Long roleId) {
        this.roleId = roleId;
        updateTimestamp();
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getTeamId() { return teamId; }
    public Long getRoleId() { return roleId; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
