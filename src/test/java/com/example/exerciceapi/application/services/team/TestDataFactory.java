package com.example.exerciceapi.application.services.team;

import application.services.teams.CreateTeamService;
import application.services.users.CreateUserService;
import domain.model.team.Team;
import domain.model.user.User;
import domain.port.users.UserRepository;

public class TestDataFactory {
    public static User createOwner(UserRepository userRepository)
    {
        return userRepository.save(User.create("testUser", "test@test.com","abctest"));
    }
    public static Team crateTeam(CreateTeamService createTeamService, String name, String slug, String description, Long owner )
    {
        return createTeamService.createTeam(name, slug, description, owner );
    }
}
