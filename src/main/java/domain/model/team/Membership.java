package domain.model.team;

import java.time.LocalDateTime;

public class Membership {

    public static final String ADMIN = "admin";
    public static final String MEMBER = "member";

    private Long id;
    private Long userId;
    private Long teamId;
    private String role;
    private Boolean isActive;
    private LocalDateTime joinedAt;
    private LocalDateTime updatedAt;

    private Membership(Long id, Long userId, Long teamId, String role,
                       Boolean isActive, LocalDateTime joinedAt, LocalDateTime updatedAt) {
        validateRole(role);
        this.id = id;
        this.userId = userId;
        this.teamId = teamId;
        this.role = role;
        this.isActive = isActive != null ? isActive : true;
        this.joinedAt = joinedAt;
        this.updatedAt = updatedAt;
    }

    // Factory 1: Create new membership
    public static Membership create(Long userId, Long teamId, String role) {
        LocalDateTime now = LocalDateTime.now();
        return new Membership(null, userId, teamId, role, true, now, now);
    }

    // Factory 2: Reconstruct from DB
    public static Membership reconstruct(Long id, Long userId, Long teamId, String role,
                                         Boolean isActive, LocalDateTime joinedAt, LocalDateTime updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("The id must not be empty for an existing membership");
        }
        return new Membership(id, userId, teamId, role, isActive, joinedAt, updatedAt);
    }

    public void promote() {
        if (ADMIN.equals(this.role)) {
            throw new IllegalStateException("Already an admin");
        }
        this.role = ADMIN;
        updateTimestamp();
    }

    public void demote() {
        if (MEMBER.equals(this.role)) {
            throw new IllegalStateException("Already a member");
        }
        this.role = MEMBER;
        updateTimestamp();
    }

    public boolean isAdmin() {
        return ADMIN.equals(this.role);
    }

    public boolean isMember() {
        return MEMBER.equals(this.role);
    }

    public void activate() {
        this.isActive = true;
        updateTimestamp();
    }

    public void deactivate() {
        this.isActive = false;
        updateTimestamp();
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    private void validateRole(String role) {
        if (role == null || (!ADMIN.equals(role) && !MEMBER.equals(role))) {
            throw new IllegalArgumentException("Role must be either 'admin' or 'member'");
        }
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getTeamId() { return teamId; }
    public String getRole() { return role; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
