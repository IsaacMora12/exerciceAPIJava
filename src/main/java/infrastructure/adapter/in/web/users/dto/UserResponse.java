package infrastructure.adapter.in.web.users.dto;

public record UserResponse(
        Long id,
        String name,
        String email,
        Boolean isActive
) {}