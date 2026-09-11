package domain.model.exercice;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MuscleTest {

    // ========== CREATE ==========
    @Test
    void shouldCreateMuscleWithValidData() {
        Muscle muscle = Muscle.create("Bicep", "Front upper arm", List.of("img1.jpg"), 10L);

        assertEquals("Bicep", muscle.getName());
        assertEquals("Front upper arm", muscle.getDescription());
        assertEquals(List.of("img1.jpg"), muscle.getImages());
        assertTrue(muscle.getIsActive());
        assertNotNull(muscle.getCreatedAt());
        assertNotNull(muscle.getUpdatedAt());
        assertEquals(10L, muscle.getUpdatedBy());
        assertNull(muscle.getId(), "ID should be null before persisting");
    }

    @Test
    void shouldCreateMuscleWithNullImages() {
        Muscle muscle = Muscle.create("Bicep", "desc", null, 1L);

        assertNotNull(muscle.getImages());
        assertTrue(muscle.getImages().isEmpty());
    }

    // ========== NAME VALIDATIONS ==========
    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                Muscle.create(null, "desc", null, 1L));
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                Muscle.create("", "desc", null, 1L));
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                Muscle.create("   ", "desc", null, 1L));
    }

    // ========== UPDATE METHODS ==========
    @Test
    void shouldUpdateName() {
        Muscle muscle = Muscle.create("Old Name", "desc", null, 1L);
        muscle.updateName("New Name");
        assertEquals("New Name", muscle.getName());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNameToInvalid() {
        Muscle muscle = Muscle.create("Name", "desc", null, 1L);
        assertThrows(IllegalArgumentException.class, () -> muscle.updateName(""));
        assertThrows(IllegalArgumentException.class, () -> muscle.updateName(null));
    }

    @Test
    void shouldUpdateDescription() {
        Muscle muscle = Muscle.create("Name", "old desc", null, 1L);
        muscle.updateDescription("new desc");
        assertEquals("new desc", muscle.getDescription());
    }

    @Test
    void shouldUpdateImages() {
        Muscle muscle = Muscle.create("Name", "desc", null, 1L);
        muscle.updateImages(List.of("img1.jpg", "img2.jpg"));
        assertEquals(List.of("img1.jpg", "img2.jpg"), muscle.getImages());
    }

    // ========== ADD/REMOVE IMAGE ==========
    @Test
    void shouldAddImage() {
        Muscle muscle = Muscle.create("Name", "desc", null, 1L);
        muscle.addImage("img1.jpg");
        assertTrue(muscle.getImages().contains("img1.jpg"));
    }

    @Test
    void shouldNotAddDuplicateImage() {
        Muscle muscle = Muscle.create("Name", "desc", List.of("img1.jpg"), 1L);
        muscle.addImage("img1.jpg");
        assertEquals(1, muscle.getImages().size());
    }

    @Test
    void shouldThrowExceptionWhenAddingEmptyImage() {
        Muscle muscle = Muscle.create("Name", "desc", null, 1L);
        assertThrows(IllegalArgumentException.class, () -> muscle.addImage(""));
        assertThrows(IllegalArgumentException.class, () -> muscle.addImage(null));
    }

    @Test
    void shouldRemoveImage() {
        Muscle muscle = Muscle.create("Name", "desc", List.of("img1.jpg", "img2.jpg"), 1L);
        muscle.removeImage("img1.jpg");
        assertFalse(muscle.getImages().contains("img1.jpg"));
    }

    // ========== ACTIVATE / DEACTIVATE ==========
    @Test
    void shouldActivateDeactivateMuscle() {
        Muscle muscle = Muscle.create("Name", "desc", null, 1L);
        assertTrue(muscle.getIsActive());

        muscle.deactivate();
        assertFalse(muscle.getIsActive());

        muscle.activate();
        assertTrue(muscle.getIsActive());
    }

    // ========== RECONSTRUCT ==========
    @Test
    void shouldReconstructMuscleFromDatabase() {
        LocalDateTime now = LocalDateTime.now();
        Muscle muscle = Muscle.reconstruct(1L, "Bicep", "desc", List.of("img.jpg"),
                true, now, now, 10L);

        assertEquals(1L, muscle.getId());
        assertEquals("Bicep", muscle.getName());
        assertTrue(muscle.getIsActive());
        assertEquals(10L, muscle.getUpdatedBy());
    }

    @Test
    void shouldThrowExceptionWhenReconstructWithNullId() {
        assertThrows(IllegalArgumentException.class, () ->
                Muscle.reconstruct(null, "Name", "desc", null, true,
                        LocalDateTime.now(), LocalDateTime.now(), 1L));
    }

    // ========== TIMESTAMP UPDATE ==========
    @Test
    void shouldUpdateTimestampOnModification() throws InterruptedException {
        Muscle muscle = Muscle.create("Name", "desc", null, 1L);
        LocalDateTime beforeUpdate = muscle.getUpdatedAt();

        Thread.sleep(10);
        muscle.updateName("New Name");

        assertTrue(muscle.getUpdatedAt().isAfter(beforeUpdate));
    }

    // ========== UNMODIFIABLE LISTS ==========
    @Test
    void shouldReturnUnmodifiableImageList() {
        Muscle muscle = Muscle.create("Name", "desc", List.of("img.jpg"), 1L);

        assertThrows(UnsupportedOperationException.class, () ->
                muscle.getImages().add("new.jpg"));
    }
}
