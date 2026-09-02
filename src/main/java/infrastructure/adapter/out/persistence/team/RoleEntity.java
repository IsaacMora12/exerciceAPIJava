package infrastructure.adapter.out.persistence.team;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "roles")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = RoleEntity.PermissionConverter.class)
    private Map<String, Boolean> permissions;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public RoleEntity() {}

    public RoleEntity(Long id, Long teamId, String name, Map<String, Boolean> permissions,
                      Boolean isDefault, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.teamId = teamId;
        this.name = name;
        this.permissions = permissions;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Map<String, Boolean> getPermissions() { return permissions; }
    public void setPermissions(Map<String, Boolean> permissions) { this.permissions = permissions; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // JPA Converter for JSON permissions
    @Converter
    public static class PermissionConverter implements AttributeConverter<Map<String, Boolean>, String> {

        @Override
        public String convertToDatabaseColumn(Map<String, Boolean> attribute) {
            if (attribute == null) return "{}";
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Boolean> entry : attribute.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }

        @Override
        public Map<String, Boolean> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isEmpty()) return new java.util.HashMap<>();
            Map<String, Boolean> map = new java.util.HashMap<>();
            // Simple JSON parsing - in production use Jackson
            String[] pairs = dbData.replace("{", "").replace("}", "").split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                if (kv.length == 2) {
                    String key = kv[0].trim().replace("\"", "");
                    Boolean value = Boolean.parseBoolean(kv[1].trim());
                    map.put(key, value);
                }
            }
            return map;
        }
    }
}
