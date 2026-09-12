package com.example.exerciceapi.application.services.exercise;

import domain.model.exercice.Exercise;
import domain.port.exercise.ExerciseRepository;
import application.services.exercise.ViewExerciseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ViewExerciseServiceIntegrationTest {

    @Autowired
    private ViewExerciseService viewExerciseService;

    @Autowired
    private ExerciseRepository exerciseRepository;

    private Exercise existingExercise;

    @BeforeEach
    void setUp() {
        existingExercise = exerciseRepository.save(
                Exercise.create("Test Exercise", List.of("desc"), "Strength",
                        List.of("Step 1"), "Barbell", 1L, List.of(2L),
                        List.of("img.jpg"), List.of("vid.mp4"), 1L));
    }

    @Test
    void shouldFindExerciseById() {
        Optional<Exercise> found = viewExerciseService.viewExercise(existingExercise.getId());

        assertTrue(found.isPresent());
        assertEquals("Test Exercise", found.get().getName());
        assertEquals(1L, found.get().getMainMuscle());
    }

    @Test
    void shouldReturnEmptyWhenExerciseNotFound() {
        Optional<Exercise> found = viewExerciseService.viewExercise(99999L);

        assertFalse(found.isPresent());
    }

    @Test
    void shouldReturnAllExerciseData() {
        Exercise found = viewExerciseService.viewExercise(existingExercise.getId()).orElseThrow();

        assertEquals(existingExercise.getId(), found.getId());
        assertEquals("Test Exercise", found.getName());
        assertEquals(List.of("desc"), found.getDescription());
        assertEquals("Strength", found.getCategory());
        assertEquals("Barbell", found.getEquipament());
        assertEquals(List.of(2L), found.getOthersMuscle());
        assertEquals(List.of("img.jpg"), found.getImages());
        assertEquals(List.of("vid.mp4"), found.getVideos());
        assertTrue(found.getIsActive());
        assertEquals(1L, found.getUpdatedBy());
    }
}
