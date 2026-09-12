package com.example.exerciceapi.application.services.exercise;

import domain.model.exercice.Exercise;
import domain.port.exercise.ExerciseRepository;
import application.services.exercise.CreateExerciseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CreateExerciseServiceIntegrationTest {

    @Autowired
    private CreateExerciseService createExerciseService;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Test
    void shouldCreateExerciseSuccessfully() {
        Exercise exercise = createExerciseService.createExercise(
                "Barbell Curl", List.of("Curl with barbell"), "Strength",
                List.of("Stand upright", "Curl up"), "Barbell", 1L,
                List.of(2L, 3L), List.of("img1.jpg"), List.of("vid1.mp4"), 10L);

        assertNotNull(exercise.getId());
        assertEquals("Barbell Curl", exercise.getName());
        assertEquals(List.of("Curl with barbell"), exercise.getDescription());
        assertEquals("Strength", exercise.getCategory());
        assertEquals("Barbell", exercise.getEquipament());
        assertEquals(1L, exercise.getMainMuscle());
        assertEquals(List.of(2L, 3L), exercise.getOthersMuscle());
        assertEquals(List.of("img1.jpg"), exercise.getImages());
        assertEquals(List.of("vid1.mp4"), exercise.getVideos());
        assertTrue(exercise.getIsActive());
        assertEquals(10L, exercise.getUpdatedBy());
    }

    @Test
    void shouldPersistExerciseInDatabase() {
        Exercise exercise = createExerciseService.createExercise(
                "Barbell Curl", List.of("desc"), "Category", null, null, 1L, null, null, null, 1L);

        Optional<Exercise> found = exerciseRepository.findById(exercise.getId());
        assertTrue(found.isPresent());
        assertEquals("Barbell Curl", found.get().getName());
    }

    @Test
    void shouldThrowExceptionWhenNameIsDuplicated() {
        createExerciseService.createExercise(
                "Barbell Curl", List.of("desc"), "Category", null, null, 1L, null, null, null, 1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                createExerciseService.createExercise(
                        "Barbell Curl", List.of("other desc"), "Other", null, null, 2L, null, null, null, 1L));

        assertTrue(exception.getMessage().contains("duplicated"));
    }

    @Test
    void shouldCreateMultipleExercisesWithDifferentNames() {
        Exercise e1 = createExerciseService.createExercise(
                "Curl", List.of("desc"), "Cat", null, null, 1L, null, null, null, 1L);
        Exercise e2 = createExerciseService.createExercise(
                "Press", List.of("desc"), "Cat", null, null, 1L, null, null, null, 1L);

        assertNotEquals(e1.getId(), e2.getId());
    }
}
