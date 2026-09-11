package com.example.exerciceapi.application.services.team;

import application.services.teams.AddMemberService;
import application.services.teams.CreateTeamService;
import application.services.teams.DemoteMemberService;
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
public class DemoteMemberServiceTest {

    @Autowired private DemoteMemberService demoteMemberService;
    @Autowired private AddMemberService addMemberService;
    @Autowired private CreateTeamService createTeamService;
    @Autowired private UserRepository userRepository;
    @Autowired private MembershipRepository membershipRepository;

    private User owner;
    User admin2;
    User member;
    private Team team;
    private Membership ownerMembership;
    private Membership admin2Membership;
    private Membership memberMembership;

    @BeforeEach
    void setUp() {
        owner = TestDataFactory.createOwner(userRepository);
        admin2 = userRepository.save(User.create("Admin2", "admin2@test.com", "password123"));
        member = userRepository.save(User.create("Member", "member@test.com", "password123"));

        team = createTeamService.createTeam("Test Team", "test-team", "Desc", owner.getId());

        // Owner is already admin from team creation
        ownerMembership = membershipRepository.findByUserIdAndTeamId(owner.getId(), team.getId()).orElseThrow();
        admin2Membership = addMemberService.addMember(admin2.getId(), team.getId(), Membership.ADMIN);
        memberMembership = addMemberService.addMember(member.getId(), team.getId(), Membership.MEMBER);
    }

    // ========== SUCCESS CASES ==========
    @Test
    void shouldDemoteAdminToMember() {
        Membership demoted = demoteMemberService.demoteMember(admin2Membership.getId(), owner.getId());

        assertEquals(Membership.MEMBER, demoted.getRole());
    }

    // ========== OWNER CANNOT BE DEMOTED ==========
    @Test
    void shouldThrowExceptionWhenDemotingOwner() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                demoteMemberService.demoteMember(ownerMembership.getId(), admin2.getId()));

        assertEquals("The team owner cannot be demoted", exception.getMessage());
    }

    // ========== REQUESTER MUST BE ADMIN ==========
    @Test
    void shouldThrowExceptionWhenRequesterIsNotAdmin() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                demoteMemberService.demoteMember(admin2Membership.getId(), member.getId()));

        assertEquals("Only admins can demote members", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenRequesterIsNotMember() {
        User outsider = userRepository.save(User.create("Outsider", "outsider@test.com", "password123"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                demoteMemberService.demoteMember(admin2Membership.getId(), outsider.getId()));

        assertEquals("Requester is not a member of this team", exception.getMessage());
    }

    // ========== ALREADY A MEMBER ==========
    @Test
    void shouldThrowExceptionWhenDemotingNonAdmin() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                demoteMemberService.demoteMember(memberMembership.getId(), owner.getId()));

        assertEquals("This member is already a regular member", exception.getMessage());
    }

    // ========== CANNOT DEMOTE LAST ADMIN ==========
    @Test
    void cannotDemoteLastAdminBecauseOwnerCheckFiresFirst() {
        // NOTE: The "last admin" validation in DemoteMemberService (line 62) is unreachable
        // in the current system because the owner is always an admin.
        // If only the owner is admin → owner check fires first.
        // If owner + X are admins → demoting X is allowed (2→1 admins).
        // This test verifies the actual behavior when only the owner is admin.
        membershipRepository.deleteById(admin2Membership.getId());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                demoteMemberService.demoteMember(ownerMembership.getId(), owner.getId()));

        assertEquals("The team owner cannot be demoted", exception.getMessage());
    }

    // ========== MEMBERSHIP NOT FOUND ==========
    @Test
    void shouldThrowExceptionWhenMembershipNotFound() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                demoteMemberService.demoteMember(999999L, owner.getId()));

        assertEquals("Membership not found", exception.getMessage());
    }
}
