package nailroad.domain.notification.service;

import java.util.UUID;
import nailroad.domain.auth.code.AuthErrorCode;
import nailroad.domain.notification.dto.response.PushSettingResponse;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.enums.AuthProvider;
import nailroad.domain.user.repository.UserRepository;
import nailroad.global.config.JpaAuditingConfig;
import nailroad.global.exception.CustomException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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

@SpringJUnitConfig(PushSettingPersistenceTest.PersistenceConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
	"spring.datasource.url=${DB_URL}",
	"spring.datasource.username=${DB_USERNAME}",
	"spring.datasource.password=${DB_PASSWORD}",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.jpa.properties.jakarta.persistence.create-database-schemas=true"
})
class PushSettingPersistenceTest {

	@DynamicPropertySource
	static void isolateTestSchema(DynamicPropertyRegistry registry) {
		String schema = "push_setting_test_" + UUID.randomUUID().toString().replace("-", "");
		registry.add("spring.jpa.properties.hibernate.default_schema", () -> schema);
	}

	@Autowired
	private PushSettingService service;
	@Autowired
	private UserRepository users;

	@AfterEach
	void cleanUp() {
		users.deleteAll();
	}

	// 푸시 설정 없이 가입한 사용자는 푸시 알림이 꺼진 상태로 저장되는지 확인한다.
	@Test
	void newUserHasPushDisabled() {
		User user = user();

		assertFalse(users.findById(user.getId()).orElseThrow().isPushEnabled());
	}

	// 푸시 알림을 켜면 사용자에게 켜진 상태가 저장되고 응답에 반영되는지 확인한다.
	@Test
	void turnsOnPush() {
		User user = user();

		PushSettingResponse response = service.change(user.getId(), true);

		assertTrue(response.pushEnabled());
		assertTrue(users.findById(user.getId()).orElseThrow().isPushEnabled());
	}

	// 켜진 푸시 알림을 끄면 꺼진 상태가 저장되는지 확인한다.
	@Test
	void turnsOffPush() {
		User user = user();
		service.change(user.getId(), true);

		PushSettingResponse response = service.change(user.getId(), false);

		assertFalse(response.pushEnabled());
		assertFalse(users.findById(user.getId()).orElseThrow().isPushEnabled());
	}

	// 존재하지 않는 사용자의 푸시 설정을 바꾸면 사용자 없음 오류를 반환하는지 확인한다.
	@Test
	void rejectsMissingUser() {
		CustomException exception = assertThrows(CustomException.class,
			() -> service.change(Long.MAX_VALUE, true));

		assertEquals(AuthErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

	private User user() {
		return users.saveAndFlush(User.builder().authProvider(AuthProvider.KAKAO).providerUserId("customer").build());
	}

	@TestConfiguration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@EntityScan(basePackageClasses = User.class)
	@EnableJpaRepositories(basePackageClasses = UserRepository.class)
	@Import({PushSettingService.class, JpaAuditingConfig.class})
	static class PersistenceConfig {
	}
}
