package domain.port.teams.in;

import domain.model.team.Membership;

public interface DemoteMemberUseCase {
    /**
     * Demote an admin to member role.
     * Rules:
     * - Only admins can demote other admins
     * - The owner cannot be demoted
     * - Team must always have at least one admin
     *
     * @param membershipId the membership to demote
     * @param requesterId the ID of the user requesting the demotion (must be admin)
     * @return the updated membership
     * @throws IllegalArgumentException if requester is not an admin or membership not found
     * @throws IllegalStateException if trying to demote owner or team would have no admins
     */
    Membership demoteMember(Long membershipId, Long requesterId);
}
