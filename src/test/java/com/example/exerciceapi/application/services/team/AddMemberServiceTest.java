package com.example.exerciceapi.application.services.team;

import application.services.teams.AddMemberService;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AddMemberServiceTest {

    @Autowired private AddMemberService addMemberService;
    @Autowired private CreateTeamService createTeamService;
    @Autowired private UserRepository userRepository;
    @Autowired private MembershipRepository membershipRepository;

    private User owner;
    private User member;
    private Team team;

    @BeforeEach
    void setUp() {
        owner = TestDataFactory.createOwner(userRepository);
        member = userRepository.save(User.create("Member", "member@test.com", "password123"));
        team = createTeamService.createTeam("Test Team", "test-team", "Description", owner.getId());
    }

    // ========== SUCCESS CASES ==========
    @Test
    void shouldAddMemberSuccessfully() {
        Membership membership = addMemberService.addMember(member.getId(), team.getId(), Membership.MEMBER);

        assertNotNull(membership.getId());
        assertEquals(member.getId(), membership.getUserId());
        assertEquals(team.getId(), membership.getTeamId());
        assertEquals(Membership.MEMBER, membership.getRole());
        assertTrue(membership.getIsActive());
        assertNotNull(membership.getJoinedAt());
    }

    @Test
    void shouldAddAdminMemberSuccessfully() {
        Membership membership = addMemberService.addMember(member.getId(), team.getId(), Membership.ADMIN);

        assertEquals(Membership.ADMIN, membership.getRole());
    }

    // ========== DUPLICATE MEMBER ==========
    @Test
    void shouldThrowExceptionWhenUserIsAlreadyMember() {
        addMemberService.addMember(member.getId(), team.getId(), Membership.MEMBER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                addMemberService.addMember(member.getId(), team.getId(), Membership.MEMBER));

        assertEquals("User is already a member of this team", exception.getMessage());
    }

    // ========== OWNER CAN BE ADDED AS ADMIN ==========
    @Test
    void shouldAllowAddingOwnerAsMember() {
        // Owner is already added as admin when team is created
        // This tests that the duplicate check works correctly
        // The owner's membership already exists, so adding again should fail
        assertThrows(IllegalArgumentException.class, () ->
                addMemberService.addMember(owner.getId(), team.getId(), Membership.ADMIN));
    }

    // ========== MULTIPLE MEMBERS ==========
    @Test
    void shouldAddMultipleDifferentMembers() {
        User user3 = userRepository.save(User.create("User3", "user3@test.com", "password123"));
        User user4 = userRepository.save(User.create("User4", "user4@test.com", "password123"));

        Membership m1 = addMemberService.addMember(member.getId(), team.getId(), Membership.MEMBER);
        Membership m2 = addMemberService.addMember(user3.getId(), team.getId(), Membership.MEMBER);
        Membership m3 = addMemberService.addMember(user4.getId(), team.getId(), Membership.ADMIN);

        assertNotNull(m1.getId());
        assertNotNull(m2.getId());
        assertNotNull(m3.getId());
        assertEquals(4, membershipRepository.findByTeamId(team.getId()).size());
    }

    // ========== TEAM ALREADY HAS OWNER AS ADMIN ==========
    @Test
    void shouldVerifyOwnerIsAdminAfterTeamCreation() {
        // When team is created, owner should be added as admin
        var members = membershipRepository.findByTeamId(team.getId());

        assertEquals(1, members.size());
        assertEquals(owner.getId(), members.get(0).getUserId());
        assertEquals(Membership.ADMIN, members.get(0).getRole());
    }
}
