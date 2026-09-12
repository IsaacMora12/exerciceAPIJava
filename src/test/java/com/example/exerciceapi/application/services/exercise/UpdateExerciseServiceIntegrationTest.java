package com.example.exerciceapi.application.services.exercise;

import domain.model.exercice.Exercise;
import domain.port.exercise.ExerciseRepository;
import application.services.exercise.UpdateExerciseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UpdateExerciseServiceIntegrationTest {

    @Autowired
    private UpdateExerciseService updateExerciseService;

    @Autowired
    private ExerciseRepository exerciseRepository;

    private Exercise existingExercise;

    @BeforeEach
    void setUp() {
        existingExercise = exerciseRepository.save(
                Exercise.create("Original Name", List.of("Original desc"), "Strength",
                        List.of("Step 1"), "Barbell", 1L, List.of(2L),
                        List.of("img.jpg"), List.of("vid.mp4"), 1L));
    }

    @Test
    void shouldUpdateExerciseSuccessfully() {
        Exercise updated = updateExerciseService.updateExercise(
                existingExercise.getId(), "Updated Name", List.of("Updated desc"), "Cardio",
                List.of("New step"), "Dumbbell", 3L,
                List.of(4L, 5L), List.of("new.jpg"), List.of("new.mp4"),
                false, 2L);

        assertEquals("Updated Name", updated.getName());
        assertEquals(List.of("Updated desc"), updated.getDescription());
        assertEquals("Cardio", updated.getCategory());
        assertEquals("Dumbbell", updated.getEquipament());
        assertEquals(3L, updated.getMainMuscle());
        assertEquals(List.of(4L, 5L), updated.getOthersMuscle());
        assertEquals(List.of("new.jpg"), updated.getImages());
        assertEquals(List.of("new.mp4"), updated.getVideos());
        assertFalse(updated.getIsActive());
        assertEquals(2L, updated.getUpdatedBy());
    }

    @Test
    void shouldPersistUpdatedExercise() {
        updateExerciseService.updateExercise(
                existingExercise.getId(), "Updated", List.of("desc"), "Cat",
                null, null, 1L, null, null, null, true, 1L);

        Exercise found = exerciseRepository.findById(existingExercise.getId()).orElseThrow();
        assertEquals("Updated", found.getName());
    }

    @Test
    void shouldThrowExceptionWhenExerciseNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                updateExerciseService.updateExercise(
                        99999L, "Name", List.of("desc"), "Cat",
                        null, null, 1L, null, null, null, true, 1L));
    }

    @Test
    void shouldThrowExceptionWhenNameIsDuplicated() {
        Exercise other = exerciseRepository.save(
                Exercise.create("Other Exercise", List.of("desc"), "Cat",
                        null, null, 2L, null, null, null, 1L));

        assertThrows(IllegalArgumentException.class, () ->
                updateExerciseService.updateExercise(
                        existingExercise.getId(), "Other Exercise", List.of("desc"), "Cat",
                        null, null, 1L, null, null, null, true, 1L));
    }

    @Test
    void shouldAllowUpdatingNameToItself() {
        Exercise updated = updateExerciseService.updateExercise(
                existingExercise.getId(), "Original Name", List.of("desc"), "Cat",
                null, null, 1L, null, null, null, true, 1L);

        assertEquals("Original Name", updated.getName());
    }
}
