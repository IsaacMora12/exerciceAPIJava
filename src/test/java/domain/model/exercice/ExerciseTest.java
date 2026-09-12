package domain.model.exercice;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExerciseTest {

    // ========== CREATE ==========
    @Test
    void shouldCreateExerciseWithValidData() {
        Exercise exercise = Exercise.create("Bicep Curl",
                List.of("Curl with barbell"), "Strength",
                List.of("Stand upright", "Curl up"), "Barbell",
                1L, List.of(2L, 3L), List.of("img1.jpg"), List.of("vid1.mp4"), 10L);

        assertEquals("Bicep Curl", exercise.getName());
        assertEquals(List.of("Curl with barbell"), exercise.getDescription());
        assertEquals("Strength", exercise.getCategory());
        assertEquals(List.of("Stand upright", "Curl up"), exercise.getInstruccion());
        assertEquals("Barbell", exercise.getEquipament());
        assertEquals(1L, exercise.getMainMuscle());
        assertEquals(List.of(2L, 3L), exercise.getOthersMuscle());
        assertEquals(List.of("img1.jpg"), exercise.getImages());
        assertEquals(List.of("vid1.mp4"), exercise.getVideos());
        assertTrue(exercise.getIsActive());
        assertNotNull(exercise.getCreatedAt());
        assertNotNull(exercise.getUpdatedAt());
        assertEquals(10L, exercise.getUpdatedBy());
        assertNull(exercise.getId(), "ID should be null before persisting");
    }

    @Test
    void shouldCreateExerciseWithNullOptionalFields() {
        Exercise exercise = Exercise.create("Bicep Curl",
                List.of("desc"), null, null, null,
                1L, null, null, null, 1L);

        assertNotNull(exercise.getOthersMuscle());
        assertNotNull(exercise.getImages());
        assertNotNull(exercise.getVideos());
        assertTrue(exercise.getOthersMuscle().isEmpty());
        assertTrue(exercise.getImages().isEmpty());
        assertTrue(exercise.getVideos().isEmpty());
        assertNull(exercise.getCategory());
        assertNull(exercise.getEquipament());
    }

    // ========== NAME VALIDATIONS ==========
    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                Exercise.create(null, List.of("desc"), null, null, null, 1L, null, null, null, 1L));
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                Exercise.create("", List.of("desc"), null, null, null, 1L, null, null, null, 1L));
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                Exercise.create("   ", List.of("desc"), null, null, null, 1L, null, null, null, 1L));
    }

    // ========== MAIN MUSCLE VALIDATIONS ==========
    @Test
    void shouldThrowExceptionWhenMainMuscleIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                Exercise.create("Name", List.of("desc"), null, null, null, null, null, null, null, 1L));
    }

    // ========== UPDATE METHODS ==========
    @Test
    void shouldUpdateName() {
        Exercise exercise = Exercise.create("Old Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        exercise.updateName("New Name");
        assertEquals("New Name", exercise.getName());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNameToInvalid() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        assertThrows(IllegalArgumentException.class, () -> exercise.updateName(""));
        assertThrows(IllegalArgumentException.class, () -> exercise.updateName(null));
    }

    @Test
    void shouldUpdateDescription() {
        Exercise exercise = Exercise.create("Name", List.of("old desc"), null, null, null, 1L, null, null, null, 1L);
        exercise.updateDescription(List.of("new desc", "extra line"));
        assertEquals(List.of("new desc", "extra line"), exercise.getDescription());
    }

    @Test
    void shouldUpdateCategory() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), "Strength", null, null, 1L, null, null, null, 1L);
        exercise.updateCategory("Cardio");
        assertEquals("Cardio", exercise.getCategory());
    }

    @Test
    void shouldUpdateInstruccion() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, List.of("step 1"), null, 1L, null, null, null, 1L);
        exercise.updateInstruccion(List.of("step 1", "step 2", "step 3"));
        assertEquals(List.of("step 1", "step 2", "step 3"), exercise.getInstruccion());
    }

    @Test
    void shouldUpdateEquipament() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, "Barbell", 1L, null, null, null, 1L);
        exercise.updateEquipament("Dumbbell");
        assertEquals("Dumbbell", exercise.getEquipament());
    }

    @Test
    void shouldUpdateMainMuscle() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        exercise.updateMainMuscle(5L);
        assertEquals(5L, exercise.getMainMuscle());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingMainMuscleToNull() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        assertThrows(IllegalArgumentException.class, () -> exercise.updateMainMuscle(null));
    }

    @Test
    void shouldUpdateOthersMuscle() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        exercise.updateOthersMuscle(List.of(2L, 3L));
        assertEquals(List.of(2L, 3L), exercise.getOthersMuscle());
    }

    @Test
    void shouldUpdateImages() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        exercise.updateImages(List.of("img1.jpg", "img2.jpg"));
        assertEquals(List.of("img1.jpg", "img2.jpg"), exercise.getImages());
    }

    @Test
    void shouldUpdateVideos() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        exercise.updateVideos(List.of("vid1.mp4"));
        assertEquals(List.of("vid1.mp4"), exercise.getVideos());
    }

    // ========== ADD/REMOVE OTHER MUSCLE ==========
    @Test
    void shouldAddOtherMuscle() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        exercise.addOtherMuscle(2L);
        assertTrue(exercise.getOthersMuscle().contains(2L));
    }

    @Test
    void shouldThrowExceptionWhenAddingDuplicateOtherMuscle() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, List.of(2L), null, null, 1L);
        assertThrows(IllegalArgumentException.class, () -> exercise.addOtherMuscle(2L));
    }

    @Test
    void shouldThrowExceptionWhenAddingNullOtherMuscle() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        assertThrows(IllegalArgumentException.class, () -> exercise.addOtherMuscle(null));
    }

    @Test
    void shouldRemoveOtherMuscle() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, List.of(2L, 3L), null, null, 1L);
        exercise.removeOtherMuscle(2L);
        assertFalse(exercise.getOthersMuscle().contains(2L));
        assertTrue(exercise.getOthersMuscle().contains(3L));
    }

    // ========== ADD/REMOVE IMAGE ==========
    @Test
    void shouldAddImage() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        exercise.addImage("img1.jpg");
        assertTrue(exercise.getImages().contains("img1.jpg"));
    }

    @Test
    void shouldNotAddDuplicateImage() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, List.of("img1.jpg"), null, 1L);
        exercise.addImage("img1.jpg");
        assertEquals(1, exercise.getImages().size());
    }

    @Test
    void shouldThrowExceptionWhenAddingEmptyImage() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        assertThrows(IllegalArgumentException.class, () -> exercise.addImage(""));
        assertThrows(IllegalArgumentException.class, () -> exercise.addImage(null));
    }

    @Test
    void shouldRemoveImage() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, List.of("img1.jpg", "img2.jpg"), null, 1L);
        exercise.removeImage("img1.jpg");
        assertFalse(exercise.getImages().contains("img1.jpg"));
    }

    // ========== ADD/REMOVE VIDEO ==========
    @Test
    void shouldAddVideo() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        exercise.addVideo("vid1.mp4");
        assertTrue(exercise.getVideos().contains("vid1.mp4"));
    }

    @Test
    void shouldNotAddDuplicateVideo() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, List.of("vid1.mp4"), 1L);
        exercise.addVideo("vid1.mp4");
        assertEquals(1, exercise.getVideos().size());
    }

    @Test
    void shouldThrowExceptionWhenAddingEmptyVideo() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        assertThrows(IllegalArgumentException.class, () -> exercise.addVideo(""));
        assertThrows(IllegalArgumentException.class, () -> exercise.addVideo(null));
    }

    @Test
    void shouldRemoveVideo() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, List.of("vid1.mp4", "vid2.mp4"), 1L);
        exercise.removeVideo("vid1.mp4");
        assertFalse(exercise.getVideos().contains("vid1.mp4"));
    }

    // ========== ACTIVATE / DEACTIVATE ==========
    @Test
    void shouldActivateExercise() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        exercise.deactivate();
        assertFalse(exercise.getIsActive());
        exercise.activate();
        assertTrue(exercise.getIsActive());
    }

    // ========== HAS METHODS ==========
    @Test
    void shouldReturnCorrectHasValues() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L,
                List.of(2L), List.of("img.jpg"), List.of("vid.mp4"), 1L);

        assertTrue(exercise.hasMainMuscle());
        assertTrue(exercise.hasOtherMuscles());
        assertTrue(exercise.hasImages());
        assertTrue(exercise.hasVideos());

        Exercise empty = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        assertFalse(empty.hasOtherMuscles());
        assertFalse(empty.hasImages());
        assertFalse(empty.hasVideos());
    }

    // ========== RECONSTRUCT ==========
    @Test
    void shouldReconstructExerciseFromDatabase() {
        LocalDateTime now = LocalDateTime.now();
        Exercise exercise = Exercise.reconstruct(1L, "Name",
                List.of("desc"), "Strength", List.of("step 1"), "Barbell",
                1L, List.of(2L), List.of("img.jpg"), List.of("vid.mp4"),
                true, now, now, 10L);

        assertEquals(1L, exercise.getId());
        assertEquals("Name", exercise.getName());
        assertEquals("Strength", exercise.getCategory());
        assertEquals("Barbell", exercise.getEquipament());
        assertTrue(exercise.getIsActive());
        assertEquals(10L, exercise.getUpdatedBy());
    }

    @Test
    void shouldThrowExceptionWhenReconstructWithNullId() {
        assertThrows(IllegalArgumentException.class, () ->
                Exercise.reconstruct(null, "Name", List.of("desc"), null, null, null,
                        1L, null, null, null, true,
                        LocalDateTime.now(), LocalDateTime.now(), 1L));
    }

    // ========== TIMESTAMP UPDATE ==========
    @Test
    void shouldUpdateTimestampOnModification() throws InterruptedException {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L, null, null, null, 1L);
        LocalDateTime beforeUpdate = exercise.getUpdatedAt();

        Thread.sleep(10);
        exercise.updateName("New Name");

        assertTrue(exercise.getUpdatedAt().isAfter(beforeUpdate));
    }

    // ========== UNMODIFIABLE LISTS ==========
    @Test
    void shouldReturnUnmodifiableLists() {
        Exercise exercise = Exercise.create("Name", List.of("desc"), null, null, null, 1L,
                List.of(2L), List.of("img.jpg"), List.of("vid.mp4"), 1L);

        assertThrows(UnsupportedOperationException.class, () ->
                exercise.getOthersMuscle().add(3L));
        assertThrows(UnsupportedOperationException.class, () ->
                exercise.getImages().add("new.jpg"));
        assertThrows(UnsupportedOperationException.class, () ->
                exercise.getVideos().add("new.mp4"));
    }
}
