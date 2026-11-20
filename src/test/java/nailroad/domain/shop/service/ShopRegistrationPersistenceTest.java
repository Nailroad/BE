package nailroad.domain.shop.service;

import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.UUID;
import nailroad.domain.shop.dto.request.ShopRegistrationRequest;
import nailroad.domain.shop.dto.response.ShopRegistrationResponse;
import nailroad.domain.shop.entity.Shop;
import nailroad.domain.shop.enums.ShopApprovalStatus;
import nailroad.domain.shop.enums.ShopStatus;
import nailroad.domain.shop.integration.AddressClient;
import nailroad.domain.shop.repository.ShopRepository;
import nailroad.domain.user.entity.User;
import nailroad.global.config.JpaAuditingConfig;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(ShopRegistrationPersistenceTest.PersistenceConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
	"spring.datasource.url=${DB_URL}",
	"spring.datasource.username=${DB_USERNAME}",
	"spring.datasource.password=${DB_PASSWORD}",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.jpa.properties.jakarta.persistence.create-database-schemas=true"
})
class ShopRegistrationPersistenceTest {

	@DynamicPropertySource
	static void isolateTestSchema(DynamicPropertyRegistry registry) {
		String schema = "shop_registration_test_" + UUID.randomUUID().toString().replace("-", "");
		registry.add("spring.jpa.properties.hibernate.default_schema", () -> schema);
	}

	@Autowired
	private ShopService service;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private TransactionTemplate transactionTemplate;
	@Autowired
	private ShopRepository shops;
	@MockitoBean
	private AddressClient coordinateClient;

	private final ShopRegistrationRequest request = new ShopRegistrationRequest(
		"민서네일", "010-0000-0000", "서울특별시 마포구 양화로 123", "2층");

	@AfterEach
	void cleanUp() {
		shops.deleteAll();
		transactionTemplate.executeWithoutResult(status -> entityManager.createQuery("delete from User").executeUpdate());
	}

	// 실제 DB에 신청자·기본정보·초기 심사 상태가 저장되고 ID가 발급되는지 확인한다.
	@Test
	void persistsNewApplication() {
		User user = user("owner");

		ShopRegistrationResponse response = service.register(user.getId(), request);

		Shop saved = shops.findById(response.shopId()).orElseThrow();
		assertEquals(user.getId(), saved.getUser().getId());
		assertEquals(request.shopName(), saved.getShopName());
		assertEquals(request.shopPhone(), saved.getShopPhone());
		assertEquals(request.shopAddress(), saved.getShopAddress());
		assertEquals(request.detailAddress(), saved.getDetailAddress());
		assertEquals(ShopApprovalStatus.PENDING, saved.getApprovalStatus());
		assertEquals(ShopStatus.PAUSED, saved.getStatus());
	}

	// 중복 조회가 계정·샵 이름·기본주소와 심사 상태 조건을 정확히 적용하는지 확인한다.
	@ParameterizedTest
	@CsvSource({"PENDING,true", "APPROVED,true", "REJECTED,false", "NEEDS_REVISION,false"})
	void queriesDuplicateByApplicantAndShop(ShopApprovalStatus status, boolean expected) {
		User user = user("owner");
		User another = user("another");
		shops.saveAndFlush(Shop.builder().user(user)
			.shopName(request.shopName()).shopAddress(request.shopAddress()).approvalStatus(status).build());
		Set<ShopApprovalStatus> blocking = Set.of(ShopApprovalStatus.PENDING, ShopApprovalStatus.APPROVED);

		assertEquals(expected, shops.existsByUser_IdAndShopNameAndShopAddressAndApprovalStatusIn(
			user.getId(), request.shopName(), request.shopAddress(), blocking));
		assertFalse(shops.existsByUser_IdAndShopNameAndShopAddressAndApprovalStatusIn(
			another.getId(), request.shopName(), request.shopAddress(), blocking));
		assertFalse(shops.existsByUser_IdAndShopNameAndShopAddressAndApprovalStatusIn(
			user.getId(), "다른 샵", request.shopAddress(), blocking));
		assertFalse(shops.existsByUser_IdAndShopNameAndShopAddressAndApprovalStatusIn(
			user.getId(), request.shopName(), "다른 기본주소", blocking));
	}

	private User user(String providerId) {
		return transactionTemplate.execute(status -> {
			User user = User.builder().authProvider("KAKAO").providerUserId(providerId).build();
			entityManager.persist(user);
			return user;
		});
	}

	@TestConfiguration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@EntityScan(basePackageClasses = {Shop.class, User.class})
	@EnableJpaRepositories(basePackageClasses = ShopRepository.class)
	@Import({ShopService.class, JpaAuditingConfig.class})
	static class PersistenceConfig {
	}
}
