package com.example.exerciceapi.application.services.team;

import application.services.teams.CreateTeamService;
import application.services.teams.UpdateTeamService;
import domain.model.team.Team;
import domain.model.user.User;
import domain.port.teams.TeamRepository;
import domain.port.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UpdateTeamServiceTest {

    @Autowired private UpdateTeamService updateTeamService;
    @Autowired private CreateTeamService createTeamService;
    @Autowired private UserRepository userRepository;
    @Autowired private TeamRepository teamRepository;

    private User owner;
    private Team team;

    @BeforeEach
    void setUp() {
        owner = TestDataFactory.createOwner(userRepository);
        team = createTeamService.createTeam("Original Team", "original-team", "Description", owner.getId());
    }

    // ========== SUCCESS CASES ==========
    @Test
    void shouldUpdateTeamName() {
        Team updated = updateTeamService.updateTeam(team.getId(), "New Name", null, null, null, null);

        assertEquals("New Name", updated.getName());
        assertEquals("original-team", updated.getSlug()); // slug unchanged
    }

    @Test
    void shouldUpdateTeamSlug() {
        Team updated = updateTeamService.updateTeam(team.getId(), null, "new-slug", null, null, null);

        assertEquals("new-slug", updated.getSlug());
    }

    @Test
    void shouldUpdateTeamDescription() {
        Team updated = updateTeamService.updateTeam(team.getId(), null, null, "New description", null, null);

        assertEquals("New description", updated.getDescription());
    }

    @Test
    void shouldUpdateMultipleFields() {
        Team updated = updateTeamService.updateTeam(team.getId(), "New Name", "new-slug", "New desc", null, null);

        assertEquals("New Name", updated.getName());
        assertEquals("new-slug", updated.getSlug());
        assertEquals("New desc", updated.getDescription());
    }

    @Test
    void shouldNotUpdateNullFields() {
        Team updated = updateTeamService.updateTeam(team.getId(), null, null, null, null, null);

        assertEquals("Original Team", updated.getName());
        assertEquals("original-team", updated.getSlug());
    }

    // ========== SLUG DUPLICATE ==========
    @Test
    void shouldHandleSlugDuplicateByIncrementing() {
        Team team2 = createTeamService.createTeam("Team 2", "team-2", "Desc", owner.getId());

        Team updated = updateTeamService.updateTeam(team2.getId(), null, "original-team", null, null, null);

        assertEquals("original-team1", updated.getSlug());
    }

    // ========== TEAM NOT FOUND ==========
    @Test
    void shouldThrowExceptionWhenTeamNotFound() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                updateTeamService.updateTeam(999999L, "New Name", null, null, null, null));

        assertEquals("Team not found", exception.getMessage());
    }

    // ========== TIMESTAMP UPDATE ==========
    @Test
    void shouldUpdateTimestamp() throws InterruptedException {
        Thread.sleep(10);
        Team updated = updateTeamService.updateTeam(team.getId(), "New Name", null, null, null, null);

        assertTrue(updated.getUpdatedAt().isAfter(team.getCreatedAt()));
    }
}
