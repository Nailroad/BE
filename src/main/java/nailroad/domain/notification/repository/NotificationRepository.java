package nailroad.domain.notification.repository;

import nailroad.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	@Query("""
		SELECT notification
		FROM Notification notification
		WHERE notification.user.id = :userId AND notification.id < :cursor
		ORDER BY notification.id DESC
		""")
	Slice<Notification> findPage(@Param("userId") Long userId, @Param("cursor") long cursor, Pageable pageable);
}
