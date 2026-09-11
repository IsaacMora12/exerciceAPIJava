package com.example.exerciceapi.application.services.team;

import application.services.teams.CreateTeamService;
import application.services.teams.DeleteTeamService;
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
public class DeleteTeamServiceTest {

    @Autowired private DeleteTeamService deleteTeamService;
    @Autowired private CreateTeamService createTeamService;
    @Autowired private UserRepository userRepository;
    @Autowired private TeamRepository teamRepository;

    private User owner;
    private Team team;

    @BeforeEach
    void setUp() {
        owner = TestDataFactory.createOwner(userRepository);
        team = createTeamService.createTeam("Team to Delete", "team-delete", "Desc", owner.getId());
    }

    @Test
    void shouldDeleteTeamSuccessfully() {
        Long teamId = team.getId();
        assertNotNull(teamRepository.findById(teamId));

        deleteTeamService.deleteTeam(teamId);

        assertTrue(teamRepository.findById(teamId).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenTeamNotFound() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                deleteTeamService.deleteTeam(999999L));

        assertEquals("Team not found", exception.getMessage());
    }

    @Test
    void shouldDeleteTeamAndAllowSlugReuse() {
        String slug = team.getSlug();
        Long teamId = team.getId();

        deleteTeamService.deleteTeam(teamId);

        // Create new team with same slug - should work now
        Team newTeam = createTeamService.createTeam("New Team", slug, "Desc", owner.getId());
        assertEquals(slug, newTeam.getSlug());
        assertNotEquals(teamId, newTeam.getId());
    }
}
