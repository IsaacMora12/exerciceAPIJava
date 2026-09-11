package com.example.exerciceapi.application.services.team;

import application.services.teams.AddMemberService;
import application.services.teams.CreateTeamService;
import application.services.teams.RemoveMemberService;
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
public class RemoveMemberServiceTest {

    @Autowired private RemoveMemberService removeMemberService;
    @Autowired private AddMemberService addMemberService;
    @Autowired private CreateTeamService createTeamService;
    @Autowired private UserRepository userRepository;
    @Autowired private MembershipRepository membershipRepository;

    private User owner;
    private User member;
    private Team team;
    private Membership memberMembership;

    @BeforeEach
    void setUp() {
        owner = TestDataFactory.createOwner(userRepository);
        member = userRepository.save(User.create("Member", "member@test.com", "password123"));
        team = createTeamService.createTeam("Test Team", "test-team", "Desc", owner.getId());
        memberMembership = addMemberService.addMember(member.getId(), team.getId(), Membership.MEMBER);
    }

    // ========== SUCCESS CASES ==========
    @Test
    void shouldRemoveMemberSuccessfully() {
        removeMemberService.removeMember(memberMembership.getId());

        assertTrue(membershipRepository.findById(memberMembership.getId()).isEmpty());
    }

    // ========== MEMBER NOT FOUND ==========
    @Test
    void shouldThrowExceptionWhenMemberNotFound() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                removeMemberService.removeMember(999999L));

        assertEquals("Membership not found", exception.getMessage());
    }

    // ========== CANNOT REMOVE LAST ADMIN ==========
    @Test
    void shouldThrowExceptionWhenRemovingLastAdmin() {
        // Owner is the only admin (added when team was created)
        Membership ownerMembership = membershipRepository.findByUserIdAndTeamId(owner.getId(), team.getId())
                .orElseThrow();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                removeMemberService.removeMember(ownerMembership.getId()));

        assertEquals("Cannot remove the last admin from the team", exception.getMessage());
    }

    @Test
    void shouldAllowRemovingAdminWhenOtherAdminsExist() {
        // Add another admin
        User admin2 = userRepository.save(User.create("Admin2", "admin2@test.com", "password123"));
        Membership admin2Membership = addMemberService.addMember(admin2.getId(), team.getId(), Membership.ADMIN);

        // Now we have 2 admins (owner + admin2), removing admin2 should work
        removeMemberService.removeMember(admin2Membership.getId());

        assertTrue(membershipRepository.findById(admin2Membership.getId()).isEmpty());
        // Owner should still be admin
        Membership ownerMembership = membershipRepository.findByUserIdAndTeamId(owner.getId(), team.getId()).orElseThrow();
        assertEquals(Membership.ADMIN, ownerMembership.getRole());
    }

    // ========== REMOVE NON-ADMIN ==========
    @Test
    void shouldAllowRemovingRegularMember() {
        removeMemberService.removeMember(memberMembership.getId());

        assertTrue(membershipRepository.findById(memberMembership.getId()).isEmpty());
        // Owner should still be there
        assertEquals(1, membershipRepository.findByTeamId(team.getId()).size());
    }
}
