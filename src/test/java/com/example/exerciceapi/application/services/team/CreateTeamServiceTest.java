package com.example.exerciceapi.application.services.team;


import application.services.teams.CreateTeamService;
import domain.model.team.Team;
import domain.model.user.User;
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
public class CreateTeamServiceTest
{
    @Autowired private CreateTeamService createTeamService;
    @Autowired private UserRepository userRepository;
    User owner;
    String name = "test";
    String slug = "testTeam";
    String description = "This is a test";
    @BeforeEach
    void setUp()
    {
        owner = TestDataFactory.createOwner(userRepository);
    }
    @Test
    void shouldCreateTeamSuccessFully()

    {

        Team createdTeam = createTeamService.createTeam(name, slug, description, owner.getId());

        assertEquals(name, createdTeam.getName());
        assertEquals(slug, createdTeam.getSlug());
        assertEquals(owner.getId(), createdTeam.getOwner());
        assertTrue(createdTeam.getIsActive());
        assertNotNull(createdTeam.getCreatedAt());

    }
    @Test
    void shouldThrowExceptionWhenOwnerDoesNotExist()
    {
        Long nonExistentOwnerId= 99999999L;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, ()-> createTeamService.createTeam(name, slug, description, nonExistentOwnerId));
        assertEquals("The owner user does not exist", exception.getMessage());


    }

}
