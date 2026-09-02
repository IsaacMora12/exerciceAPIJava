package domain.model.team;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TeamTest {
    @Test
    void shouldCreateTeamWithValidData() {
        String name = "test";
        String slug = "testTeam";
        String description = "test abc";
        Long owner = 1L;

        Team team = Team.create(name, slug, owner, description);

        assertEquals(name, team.getName());
        assertEquals(slug, team.getSlug());
        assertEquals(owner, team.getOwner());
        assertTrue(team.getIsActive());
        assertNotNull(team.getCreatedAt());
    }
}
