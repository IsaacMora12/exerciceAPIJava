package infrastructure.adapter.out.persistence.user;

import domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserPersistenceMapper {

    private static final Logger log = LoggerFactory.getLogger(UserPersistenceMapper.class);

    // Domain -> ORM
    public static UserEntity toEntity(User domain) {
        if (domain == null) return null;
        UserEntity entity = new UserEntity(
                domain.getId(),
                domain.getName(),
                domain.getEmail(),
                domain.getPassword(),
                domain.getIsActive(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
        log.debug("Mapping domain user to entity: id={}, email={}", domain.getId(), domain.getEmail());
        return entity;
    }

    // ORM -> Domain
    public static User toDomain(UserEntity entity) {
        if (entity == null) return null;
        log.debug("Mapping entity to domain user: id={}, email={}", entity.getId(), entity.getEmail());
        return User.reconstruct(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
