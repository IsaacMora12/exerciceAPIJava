package infrastructure.adapter.out.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, Long>
{
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByIsActive(Boolean isActive);

}
