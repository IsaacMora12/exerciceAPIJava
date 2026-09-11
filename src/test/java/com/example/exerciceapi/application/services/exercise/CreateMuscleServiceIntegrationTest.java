package com.example.exerciceapi.application.services.exercise;

import domain.model.exercice.Muscle;
import domain.port.exercise.MuscleRepository;
import application.services.exercise.CreateMuscleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CreateMuscleServiceIntegrationTest {

    @Autowired
    private CreateMuscleService createMuscleService;

    @Autowired
    private MuscleRepository muscleRepository;

    @Test
    void shouldCreateMuscleSuccessfully() {
        Muscle muscle = createMuscleService.createMuscle(
                "Bicep", "Front upper arm muscle", List.of("img1.jpg"), 10L);

        assertNotNull(muscle.getId());
        assertEquals("Bicep", muscle.getName());
        assertEquals("Front upper arm muscle", muscle.getDescription());
        assertEquals(List.of("img1.jpg"), muscle.getImages());
        assertTrue(muscle.getIsActive());
        assertEquals(10L, muscle.getUpdatedBy());
    }

    @Test
    void shouldPersistMuscleInDatabase() {
        Muscle muscle = createMuscleService.createMuscle(
                "Bicep", "desc", List.of("img1.jpg"),  1L);

        Optional<Muscle> found = muscleRepository.findById(muscle.getId());
        assertTrue(found.isPresent());
        assertEquals("Bicep", found.get().getName());
    }

    @Test
    void shouldThrowExceptionWhenNameIsDuplicated() {
        createMuscleService.createMuscle("Bicep", "desc", List.of("img1.jpg"), 1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                createMuscleService.createMuscle("Bicep", "other desc", null, 1L));

        assertTrue(exception.getMessage().contains("duplicated"));
    }

    @Test
    void shouldCreateMultipleMusclesWithDifferentNames() {
        Muscle m1 = createMuscleService.createMuscle("Bicep", "desc", List.of("img1.jpg"), 1L);
        Muscle m2 = createMuscleService.createMuscle("Tricep", "desc", List.of("img1.jpg"), 1L);

        assertNotEquals(m1.getId(), m2.getId());
    }

    @Test
    void shouldCreateMuscleWithNullImages() {
        Muscle muscle = createMuscleService.createMuscle("Bicep", "desc", null, 1L);

        assertNotNull(muscle.getImages());
        assertTrue(muscle.getImages().isEmpty());
    }
}
