package com.example.exerciceapi.application.services.exercise;

import domain.model.exercice.Exercise;
import domain.model.exercice.Muscle;
import domain.port.exercise.ExerciseRepository;
import domain.port.exercise.MuscleRepository;
import application.services.exercise.DeleteMuscleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DeleteMuscleServiceIntegrationTest {

    @Autowired
    private DeleteMuscleService deleteMuscleService;

    @Autowired
    private MuscleRepository muscleRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    private Muscle existingMuscle;

    @BeforeEach
    void setUp() {
        existingMuscle = muscleRepository.save(
                Muscle.create("Tricep", "Back of arm", null, 1L));
    }

    @Test
    void shouldDeleteMuscleSuccessfully() {
        deleteMuscleService.deleteMuscle(existingMuscle.getId());

        assertTrue(muscleRepository.findById(existingMuscle.getId()).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenMuscleNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                deleteMuscleService.deleteMuscle(99999L));
    }

    @Test
    void shouldThrowExceptionWhenMuscleIsUsedByExercise() {
        // Create an exercise that references this muscle
        exerciseRepository.save(
                Exercise.create("Exercise Using Muscle", "desc", existingMuscle.getId(),
                        null, null, null, 1L));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                deleteMuscleService.deleteMuscle(existingMuscle.getId()));

        assertTrue(exception.getMessage().contains("in use"));
    }

    @Test
    void shouldThrowExceptionWhenMuscleIsUsedAsSecondaryMuscle() {
        // Create an exercise that references this muscle as secondary
        exerciseRepository.save(
                Exercise.create("Exercise With Secondary", "desc", 99L,
                        List.of(existingMuscle.getId()), null, null, 1L));

        assertThrows(IllegalArgumentException.class, () ->
                deleteMuscleService.deleteMuscle(existingMuscle.getId()));
    }

    @Test
    void shouldDeleteOnlySpecifiedMuscle() {
        Muscle other = muscleRepository.save(
                Muscle.create("Lat", "Back muscle", null, 1L));

        deleteMuscleService.deleteMuscle(existingMuscle.getId());

        assertTrue(muscleRepository.findById(existingMuscle.getId()).isEmpty());
        assertTrue(muscleRepository.findById(other.getId()).isPresent());
    }
}
