package com.example.exerciceapi.application.services.exercise;

import domain.model.exercice.Muscle;
import domain.port.exercise.MuscleRepository;
import application.services.exercise.UpdateMuscleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UpdateMuscleServiceIntegrationTest {

    @Autowired
    private UpdateMuscleService updateMuscleService;

    @Autowired
    private MuscleRepository muscleRepository;

    private Muscle existingMuscle;

    @BeforeEach
    void setUp() {
        existingMuscle = muscleRepository.save(
                Muscle.create("Original", "Original desc", List.of("img.jpg"), 1L));
    }

    @Test
    void shouldUpdateMuscleSuccessfully() {
        Muscle updated = updateMuscleService.updateMuscle(
                existingMuscle.getId(), "Updated", "Updated desc",
                List.of("new.jpg"), false, 2L);

        assertEquals("Updated", updated.getName());
        assertEquals("Updated desc", updated.getDescription());
        assertEquals(List.of("new.jpg"), updated.getImages());
        assertFalse(updated.getIsActive());
        assertEquals(2L, updated.getUpdatedBy());
    }

    @Test
    void shouldPersistUpdatedMuscle() {
        updateMuscleService.updateMuscle(
                existingMuscle.getId(), "Updated", "desc", null, true, 1L);

        Muscle found = muscleRepository.findById(existingMuscle.getId()).orElseThrow();
        assertEquals("Updated", found.getName());
    }

    @Test
    void shouldThrowExceptionWhenMuscleNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                updateMuscleService.updateMuscle(
                        99999L, "Name", "desc", null, true, 1L));
    }

    @Test
    void shouldThrowExceptionWhenNameIsDuplicated() {
        Muscle other = muscleRepository.save(
                Muscle.create("Other Muscle", "desc", null, 1L));

        assertThrows(IllegalArgumentException.class, () ->
                updateMuscleService.updateMuscle(
                        existingMuscle.getId(), "Other Muscle", "desc",
                        null, true, 1L));
    }

    @Test
    void shouldAllowUpdatingNameToItself() {
        Muscle updated = updateMuscleService.updateMuscle(
                existingMuscle.getId(), "Original", "desc", null, true, 1L);

        assertEquals("Original", updated.getName());
    }

    @Test
    void shouldActivateDeactivateMuscle() {
        Muscle deactivated = updateMuscleService.updateMuscle(
                existingMuscle.getId(), "Original", "desc", null, false, 1L);
        assertFalse(deactivated.getIsActive());

        Muscle activated = updateMuscleService.updateMuscle(
                existingMuscle.getId(), "Original", "desc", null, true, 1L);
        assertTrue(activated.getIsActive());
    }
}
