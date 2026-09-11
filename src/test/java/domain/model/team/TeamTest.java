package domain.model.team;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TeamTest {

    // ========== CREATE ==========
    @Test
    void shouldCreateTeamWithValidData() {
        Team team = Team.create("Engineering", "engineering", 1L, "Backend team");

        assertEquals("Engineering", team.getName());
        assertEquals("engineering", team.getSlug());
        assertEquals(1L, team.getOwner());
        assertEquals("Backend team", team.getDescription());
        assertTrue(team.getIsActive());
        assertNotNull(team.getCreatedAt());
        assertNotNull(team.getUpdatedAt());
        assertNull(team.getId(), "ID should be null before persisting");
    }

    // ========== NAME VALIDATIONS ==========
    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                Team.create(null, "slug", 1L, "desc"));
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                Team.create("", "slug", 1L, "desc"));
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                Team.create("   ", "slug", 1L, "desc"));
    }

    // ========== SLUG VALIDATIONS ==========
    @Test
    void shouldThrowExceptionWhenSlugIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                Team.create("Team", null, 1L, "desc"));
    }

    @Test
    void shouldThrowExceptionWhenSlugIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                Team.create("Team", "", 1L, "desc"));
    }

    // ========== OWNER VALIDATIONS ==========
    @Test
    void shouldThrowExceptionWhenOwnerIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                Team.create("Team", "slug", null, "desc"));
    }

    // ========== DESCRIPTION ==========
    @Test
    void shouldAllowNullDescription() {
        Team team = Team.create("Team", "slug", 1L, null);
        assertNull(team.getDescription());
    }

    // ========== UPDATE METHODS ==========
    @Test
    void shouldUpdateName() {
        Team team = Team.create("Team", "slug", 1L, "desc");
        team.updateName("New Name");
        assertEquals("New Name", team.getName());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNameToInvalid() {
        Team team = Team.create("Team", "slug", 1L, "desc");
        assertThrows(IllegalArgumentException.class, () -> team.updateName(""));
    }

    @Test
    void shouldUpdateSlug() {
        Team team = Team.create("Team", "slug", 1L, "desc");
        team.updateSlug("new-slug");
        assertEquals("new-slug", team.getSlug());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingSlugToInvalid() {
        Team team = Team.create("Team", "slug", 1L, "desc");
        assertThrows(IllegalArgumentException.class, () -> team.updateSlug(""));
    }

    @Test
    void shouldUpdateDescription() {
        Team team = Team.create("Team", "slug", 1L, "old desc");
        team.updateDescription("new desc");
        assertEquals("new desc", team.getDescription());
    }

    @Test
    void shouldAllowNullDescriptionOnUpdate() {
        Team team = Team.create("Team", "slug", 1L, "old desc");
        team.updateDescription(null);
        assertNull(team.getDescription());
    }

    // ========== RECONSTRUCT ==========
    @Test
    void shouldReconstructTeamFromDatabase() {
        LocalDateTime now = LocalDateTime.now();
        Team team = Team.reconstruct(1L, "Team", "team-slug", "desc", 1L, true, now, now);

        assertEquals(1L, team.getId());
        assertEquals("Team", team.getName());
        assertEquals("team-slug", team.getSlug());
        assertEquals(1L, team.getOwner());
        assertTrue(team.getIsActive());
    }

    @Test
    void shouldThrowExceptionWhenReconstructWithNullId() {
        assertThrows(IllegalArgumentException.class, () ->
                Team.reconstruct(null, "Team", "slug", "desc", 1L, true,
                        LocalDateTime.now(), LocalDateTime.now()));
    }

    // ========== TIMESTAMP UPDATE ==========
    @Test
    void shouldUpdateTimestampOnModification() throws InterruptedException {
        Team team = Team.create("Team", "slug", 1L, "desc");
        LocalDateTime beforeUpdate = team.getUpdatedAt();

        Thread.sleep(10); // Small delay to ensure timestamp differs
        team.updateName("New Name");

        assertTrue(team.getUpdatedAt().isAfter(beforeUpdate));
    }
}
