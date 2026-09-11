package com.example.exerciceapi.application.services.team;


import application.services.teams.CreateTeamService;
import domain.model.team.Membership;
import domain.model.team.Team;
import domain.model.user.User;
import domain.port.teams.MembershipRepository;
import domain.port.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CreateTeamServiceTest
{
    @Autowired private CreateTeamService createTeamService;
    @Autowired private UserRepository userRepository;
    @Autowired private MembershipRepository membershipRepository;
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
        Long teamId = createdTeam.getId();
        assertEquals(name, createdTeam.getName());
        assertEquals(slug, createdTeam.getSlug());
        assertEquals(owner.getId(), createdTeam.getOwner());
        assertTrue(createdTeam.getIsActive());
        assertNotNull(createdTeam.getCreatedAt());
        List<Membership> members = membershipRepository.findByTeamId(teamId);

        assertNotNull(members);
        assertEquals(1, members.size());
        assertEquals(owner.getId(), members.get(0).getUserId());
        assertEquals(Membership.ADMIN, members.get(0).getRole());

    }
    @Test
    void shouldThrowExceptionWhenOwnerDoesNotExist()
    {
        Long nonExistentOwnerId= 99999999L;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, ()-> createTeamService.createTeam(name, slug, description, nonExistentOwnerId));
        assertEquals("The owner user does not exist", exception.getMessage());


    }
    @Test
    void shouldCreateTeamDuplicateNameSuccess()
    {
        Team createdTeam = createTeamService.createTeam(name, slug, description, owner.getId());
        Team createdTeam2 = createTeamService.createTeam(name, slug, description, owner.getId());
        Team createdTeam3 = createTeamService.createTeam(name, slug, description, owner.getId());
        assertEquals(slug, createdTeam.getSlug());
        assertEquals("testTeam1", createdTeam2.getSlug());
        assertEquals("testTeam2", createdTeam3.getSlug());
    }

}
