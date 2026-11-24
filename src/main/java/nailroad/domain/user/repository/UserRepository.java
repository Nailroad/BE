package nailroad.domain.user.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByAuthProviderAndProviderUserId(AuthProvider authProvider, String providerUserId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select user from User user where user.id = :id")
	Optional<User> findByIdForUpdate(@Param("id") Long id);
}
