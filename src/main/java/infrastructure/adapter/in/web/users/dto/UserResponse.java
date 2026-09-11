package infrastructure.adapter.in.web.users.dto;

import infrastructure.adapter.out.persistence.user.UserEntity;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserEntity.Rol rol,
        Boolean isActive
) {}