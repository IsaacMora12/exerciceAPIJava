package com.example.exerciceapi.application.services.exercise;

import domain.model.exercice.Exercise;
import domain.port.exercise.ExerciseRepository;
import application.services.exercise.DeleteExerciseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DeleteExerciseServiceIntegrationTest {

    @Autowired
    private DeleteExerciseService deleteExerciseService;

    @Autowired
    private ExerciseRepository exerciseRepository;

    private Exercise existingExercise;

    @BeforeEach
    void setUp() {
        existingExercise = exerciseRepository.save(
                Exercise.create("To Delete", List.of("desc"), "Cat", null, null, 1L, null, null, null, 1L));
    }

    @Test
    void shouldDeleteExerciseSuccessfully() {
        deleteExerciseService.deleteExercise(existingExercise.getId());

        assertTrue(exerciseRepository.findById(existingExercise.getId()).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenExerciseNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                deleteExerciseService.deleteExercise(99999L));
    }

    @Test
    void shouldDeleteOnlySpecifiedExercise() {
        Exercise other = exerciseRepository.save(
                Exercise.create("Other", List.of("desc"), "Cat", null, null, 1L, null, null, null, 1L));

        deleteExerciseService.deleteExercise(existingExercise.getId());

        assertTrue(exerciseRepository.findById(existingExercise.getId()).isEmpty());
        assertTrue(exerciseRepository.findById(other.getId()).isPresent());
    }
}
