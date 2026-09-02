package domain.model.team;

import java.time.LocalDateTime;

public class Invitation {

    public enum Status {
        PENDING,
        ACCEPTED,
        EXPIRED,
        CANCELLED
    }

    private Long id;
    private Long teamId;
    private String email;
    private Long roleId;
    private String token;
    private Status status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Invitation(Long id, Long teamId, String email, Long roleId, String token,
                       Status status, LocalDateTime expiresAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateEmail(email);
        this.id = id;
        this.teamId = teamId;
        this.email = email;
        this.roleId = roleId;
        this.token = token;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Factory 1: Create new invitation
    public static Invitation create(Long teamId, String email, Long roleId, String token, LocalDateTime expiresAt) {
        LocalDateTime now = LocalDateTime.now();
        return new Invitation(null, teamId, email, roleId, token, Status.PENDING, expiresAt, now, now);
    }

    // Factory 2: Reconstruct from DB
    public static Invitation reconstruct(Long id, Long teamId, String email, Long roleId, String token,
                                         Status status, LocalDateTime expiresAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("The id must not be empty for an existing invitation");
        }
        return new Invitation(id, teamId, email, roleId, token, status, expiresAt, createdAt, updatedAt);
    }

    public void accept() {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("Only pending invitations can be accepted");
        }
        if (LocalDateTime.now().isAfter(this.expiresAt)) {
            this.status = Status.EXPIRED;
            throw new IllegalStateException("This invitation has expired");
        }
        this.status = Status.ACCEPTED;
        updateTimestamp();
    }

    public void cancel() {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("Only pending invitations can be cancelled");
        }
        this.status = Status.CANCELLED;
        updateTimestamp();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("The invitation email must not be empty");
        }
    }

    // Getters
    public Long getId() { return id; }
    public Long getTeamId() { return teamId; }
    public String getEmail() { return email; }
    public Long getRoleId() { return roleId; }
    public String getToken() { return token; }
    public Status getStatus() { return status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
