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
                "Barbell Curl", "Curl with barbell", 1L,
                List.of(2L, 3L), List.of("img1.jpg"), List.of("vid1.mp4"), 10L);

        assertNotNull(exercise.getId());
        assertEquals("Barbell Curl", exercise.getName());
        assertEquals("Curl with barbell", exercise.getDescription());
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
                "Barbell Curl", "desc", 1L, null, null, null, 1L);

        Optional<Exercise> found = exerciseRepository.findById(exercise.getId());
        assertTrue(found.isPresent());
        assertEquals("Barbell Curl", found.get().getName());
    }

    @Test
    void shouldThrowExceptionWhenNameIsDuplicated() {
        createExerciseService.createExercise(
                "Barbell Curl", "desc", 1L, null, null, null, 1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                createExerciseService.createExercise(
                        "Barbell Curl", "other desc", 2L, null, null, null, 1L));

        assertTrue(exception.getMessage().contains("duplicated"));
    }

    @Test
    void shouldCreateMultipleExercisesWithDifferentNames() {
        Exercise e1 = createExerciseService.createExercise(
                "Curl", "desc", 1L, null, null, null, 1L);
        Exercise e2 = createExerciseService.createExercise(
                "Press", "desc", 1L, null, null, null, 1L);

        assertNotEquals(e1.getId(), e2.getId());
    }
}
