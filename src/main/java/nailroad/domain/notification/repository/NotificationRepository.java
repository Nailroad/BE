package nailroad.domain.notification.repository;

import java.util.Collection;
import nailroad.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

	boolean existsByUser_IdAndReadFalse(Long userId);

	/** 이미 읽은 알림도 갱신 대상에 포함하므로, 돌려준 개수가 요청한 ID 수보다 적으면 내 알림이 아니거나 없는 알림이 섞여 있다. */
	@Modifying
	@Query("""
		UPDATE Notification notification
		SET notification.read = true
		WHERE notification.id IN :notificationIds AND notification.user.id = :userId
		""")
	int markAsRead(@Param("userId") Long userId, @Param("notificationIds") Collection<Long> notificationIds);
}
