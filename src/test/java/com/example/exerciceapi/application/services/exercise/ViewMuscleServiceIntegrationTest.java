package com.example.exerciceapi.application.services.exercise;

import domain.model.exercice.Muscle;
import domain.port.exercise.MuscleRepository;
import application.services.exercise.ViewMuscleService;
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
class ViewMuscleServiceIntegrationTest {

    @Autowired
    private ViewMuscleService viewMuscleService;

    @Autowired
    private MuscleRepository muscleRepository;

    private Muscle existingMuscle;

    @BeforeEach
    void setUp() {
        existingMuscle = muscleRepository.save(
                Muscle.create("Bicep", "Front upper arm", List.of("img.jpg"), 1L));
    }

    @Test
    void shouldFindMuscleById() {
        Optional<Muscle> found = viewMuscleService.viewMuscle(existingMuscle.getId());

        assertTrue(found.isPresent());
        assertEquals("Bicep", found.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenMuscleNotFound() {
        Optional<Muscle> found = viewMuscleService.viewMuscle(99999L);

        assertFalse(found.isPresent());
    }

    @Test
    void shouldReturnAllMuscleData() {
        Muscle found = viewMuscleService.viewMuscle(existingMuscle.getId()).orElseThrow();

        assertEquals(existingMuscle.getId(), found.getId());
        assertEquals("Bicep", found.getName());
        assertEquals("Front upper arm", found.getDescription());
        assertEquals(List.of("img.jpg"), found.getImages());
        assertTrue(found.getIsActive());
        assertEquals(1L, found.getUpdatedBy());
    }
}
