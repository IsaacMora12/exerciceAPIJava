package infrastructure.adapter.out.persistence.user;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity implements UserDetails
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private Rol rol;
    @Column(name="is_active", nullable = false)
    private Boolean isActive;
    @Column(name="create_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name="update_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserEntity() {
        // Constructor vacío requerido por Hibernate/JPA para instanciar entidades desde la BD
    }

    public UserEntity(Long id, String name, String email, String password, Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt, Rol rol) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    // Getters y Setters nesesari  JPA
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(rol.name()));
    }

    public String getPassword() { return password; }

    @Override
    public String getUsername() {
        return email;
    }

    public enum Rol {
        USER, ADMIN
    }

    public void setPassword(String password) { this.password = password; }
    public Rol getRol() {return rol;}
    public void setRol(Rol rol) {this.rol = rol;}
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

