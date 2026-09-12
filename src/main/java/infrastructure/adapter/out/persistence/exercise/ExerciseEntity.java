package infrastructure.adapter.out.persistence.exercise;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exercises")
public class ExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    @Convert(converter = ListStringConverter.class)
    private List<String> description;

    private String category;

    @Column
    @Convert(converter = ListStringConverter.class)
    private List<String> instruccion;

    private String equipament;

    @Column(name = "main_muscle", nullable = false)
    private Long mainMuscle;

    @Column(name = "others_muscle")
    @Convert(converter = ListLongConverter.class)
    private List<Long> othersMuscle;

    @Column
    @Convert(converter = ListStringConverter.class)
    private List<String> images;

    @Column
    @Convert(converter = ListStringConverter.class)
    private List<String> videos;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;

    protected ExerciseEntity() {}

    public ExerciseEntity(Long id, String name, List<String> description, String category,
                          List<String> instruccion, String equipament,
                          Long mainMuscle, List<Long> othersMuscle,
                          List<String> images, List<String> videos,
                          Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt,
                          Long updatedBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.instruccion = instruccion;
        this.equipament = equipament;
        this.mainMuscle = mainMuscle;
        this.othersMuscle = othersMuscle;
        this.images = images;
        this.videos = videos;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getDescription() { return description; }
    public void setDescription(List<String> description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<String> getInstruccion() { return instruccion; }
    public void setInstruccion(List<String> instruccion) { this.instruccion = instruccion; }
    public String getEquipament() { return equipament; }
    public void setEquipament(String equipament) { this.equipament = equipament; }
    public Long getMainMuscle() { return mainMuscle; }
    public void setMainMuscle(Long mainMuscle) { this.mainMuscle = mainMuscle; }
    public List<Long> getOthersMuscle() { return othersMuscle; }
    public void setOthersMuscle(List<Long> othersMuscle) { this.othersMuscle = othersMuscle; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public List<String> getVideos() { return videos; }
    public void setVideos(List<String> videos) { this.videos = videos; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
}
