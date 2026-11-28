package nailroad.domain.notification.service;

import java.util.List;
import java.util.UUID;
import nailroad.domain.notification.dto.response.NotificationQueryResponse.ListResponse;
import nailroad.domain.notification.entity.Notification;
import nailroad.domain.notification.enums.NotificationType;
import nailroad.domain.notification.repository.NotificationRepository;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.enums.AuthProvider;
import nailroad.domain.user.repository.UserRepository;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.config.JpaAuditingConfig;
import nailroad.global.exception.CustomException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(NotificationPersistenceTest.PersistenceConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
	"spring.datasource.url=${DB_URL}",
	"spring.datasource.username=${DB_USERNAME}",
	"spring.datasource.password=${DB_PASSWORD}",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.jpa.properties.jakarta.persistence.create-database-schemas=true"
})
class NotificationPersistenceTest {

	@DynamicPropertySource
	static void isolateTestSchema(DynamicPropertyRegistry registry) {
		String schema = "notification_test_" + UUID.randomUUID().toString().replace("-", "");
		registry.add("spring.jpa.properties.hibernate.default_schema", () -> schema);
	}

	@Autowired
	private NotificationService service;
	@Autowired
	private NotificationRepository notifications;
	@Autowired
	private UserRepository users;

	@AfterEach
	void cleanUp() {
		notifications.deleteAll();
		users.deleteAll();
	}

	// 알림 목록이 요청한 사용자의 알림만 최신순으로 담고, 알림 정보를 그대로 내려주는지 확인한다.
	@Test
	void listsOwnNotificationsNewestFirst() {
		User customer = user("customer");
		Notification first = notification(customer);
		Notification second = notification(customer);
		notification(user("another"));

		ListResponse response = service.getList(customer.getId(), null, null);

		assertEquals(List.of(second.getId(), first.getId()),
			response.items().stream().map(ListResponse.Item::notificationId).toList());
		assertFalse(response.hasNext());
		assertNull(response.nextCursor());
		ListResponse.Item item = response.items().getFirst();
		assertEquals(NotificationType.RESERVATION_CONFIRMED, item.type());
		assertEquals("예약이 확정되었어요!", item.title());
		assertEquals("손네일 10월 29일(수)", item.body());
		assertEquals(17L, item.targetId());
		assertFalse(item.isRead());
		assertNotNull(item.createdAt());
	}

	// 커서로 다음 페이지를 요청하면 이전 페이지의 마지막 알림 다음부터 이어서 조회하는지 확인한다.
	@Test
	void pagesWithCursor() {
		User customer = user("customer");
		Notification oldest = notification(customer);
		Notification middle = notification(customer);
		Notification newest = notification(customer);

		ListResponse firstPage = service.getList(customer.getId(), null, 2);
		ListResponse secondPage = service.getList(customer.getId(), firstPage.nextCursor(), 2);

		assertEquals(List.of(newest.getId(), middle.getId()),
			firstPage.items().stream().map(ListResponse.Item::notificationId).toList());
		assertTrue(firstPage.hasNext());
		assertEquals(middle.getId(), firstPage.nextCursor());
		assertEquals(List.of(oldest.getId()),
			secondPage.items().stream().map(ListResponse.Item::notificationId).toList());
		assertFalse(secondPage.hasNext());
		assertNull(secondPage.nextCursor());
	}

	// 페이지 크기나 커서가 허용 범위를 벗어나면 잘못된 요청 오류를 반환하는지 확인한다.
	@ParameterizedTest
	@CsvSource(value = {"null, 0", "null, 101", "0, 20"}, nullValues = "null")
	void rejectsInvalidPage(Long cursor, Integer size) {
		User customer = user("customer");

		CustomException exception = assertThrows(CustomException.class,
			() -> service.getList(customer.getId(), cursor, size));

		assertEquals(GlobalErrorCode.BAD_REQUEST, exception.getErrorCode());
	}

	private User user(String providerId) {
		return users.saveAndFlush(User.builder().authProvider(AuthProvider.KAKAO).providerUserId(providerId).build());
	}

	private Notification notification(User user) {
		return notifications.saveAndFlush(Notification.builder()
			.user(user)
			.type(NotificationType.RESERVATION_CONFIRMED)
			.title("예약이 확정되었어요!")
			.body("손네일 10월 29일(수)")
			.targetId(17L)
			.build());
	}

	@TestConfiguration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@EntityScan(basePackageClasses = {Notification.class, User.class})
	@EnableJpaRepositories(basePackageClasses = {NotificationRepository.class, UserRepository.class})
	@Import({NotificationService.class, JpaAuditingConfig.class})
	static class PersistenceConfig {
	}
}
