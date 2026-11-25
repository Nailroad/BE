package nailroad.domain.shop.service;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import nailroad.domain.shop.code.ShopErrorCode;
import nailroad.domain.shop.entity.Shop;
import nailroad.domain.shop.enums.ShopApprovalStatus;
import nailroad.domain.shop.repository.ShopLikeRepository;
import nailroad.domain.shop.repository.ShopRepository;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.enums.AuthProvider;
import nailroad.global.config.JpaAuditingConfig;
import nailroad.global.exception.CustomException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@SpringJUnitConfig(ShopLikePersistenceTest.PersistenceConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
	"spring.datasource.url=${DB_URL}",
	"spring.datasource.username=${DB_USERNAME}",
	"spring.datasource.password=${DB_PASSWORD}",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.jpa.properties.jakarta.persistence.create-database-schemas=true"
})
class ShopLikePersistenceTest {

	@DynamicPropertySource
	static void isolateTestSchema(DynamicPropertyRegistry registry) {
		String schema = "shop_like_test_" + UUID.randomUUID().toString().replace("-", "");
		registry.add("spring.jpa.properties.hibernate.default_schema", () -> schema);
	}

	@Autowired
	private ShopLikeService service;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private TransactionTemplate transactionTemplate;
	@Autowired
	private ShopRepository shops;
	@MockitoSpyBean
	private ShopLikeRepository shopLikes;

	@AfterEach
	void cleanUp() {
		shopLikes.deleteAll();
		shops.deleteAll();
		transactionTemplate.executeWithoutResult(status -> entityManager.createQuery("delete from User").executeUpdate());
	}

	// 승인된 샵을 찜하면 사용자와 샵을 연결한 찜 행이 저장되는지 확인한다.
	@Test
	void likesApprovedShop() {
		User customer = user("customer");
		Shop shop = shop(user("owner"), ShopApprovalStatus.APPROVED);

		service.like(customer.getId(), shop.getId());

		assertTrue(shopLikes.existsByUser_IdAndShop_Id(customer.getId(), shop.getId()));
		assertEquals(1, shopLikes.count());
	}

	// 이미 찜한 샵을 다시 찜해도 오류 없이 찜 행이 하나만 남는지 확인한다.
	@Test
	void likingTwiceKeepsSingleLike() {
		User customer = user("customer");
		Shop shop = shop(user("owner"), ShopApprovalStatus.APPROVED);
		service.like(customer.getId(), shop.getId());

		assertDoesNotThrow(() -> service.like(customer.getId(), shop.getId()));

		assertEquals(1, shopLikes.count());
	}

	// 존재 확인 직후 다른 요청이 먼저 찜을 저장한 경우에도 오류 없이 찜 행이 하나만 남는지 확인한다.
	@Test
	void likingAfterConcurrentInsertKeepsSingleLike() {
		User customer = user("customer");
		Shop shop = shop(user("owner"), ShopApprovalStatus.APPROVED);
		service.like(customer.getId(), shop.getId());
		doReturn(false).when(shopLikes).existsByUser_IdAndShop_Id(customer.getId(), shop.getId());

		assertDoesNotThrow(() -> service.like(customer.getId(), shop.getId()));

		assertEquals(1, shopLikes.count());
	}

	// 승인되지 않은 샵은 공개되지 않은 샵으로 보고 찜하지 않는지 확인한다.
	@ParameterizedTest
	@EnumSource(value = ShopApprovalStatus.class, names = {"PENDING", "NEEDS_REVISION", "REJECTED"})
	void rejectsUnapprovedShop(ShopApprovalStatus approvalStatus) {
		User customer = user("customer");
		Shop shop = shop(user("owner"), approvalStatus);

		CustomException exception = assertThrows(CustomException.class,
			() -> service.like(customer.getId(), shop.getId()));

		assertEquals(ShopErrorCode.NOT_FOUND, exception.getErrorCode());
		assertEquals(0, shopLikes.count());
	}

	// 존재하지 않는 샵을 찜하면 샵 없음 오류를 반환하는지 확인한다.
	@Test
	void rejectsMissingShop() {
		User customer = user("customer");

		CustomException exception = assertThrows(CustomException.class,
			() -> service.like(customer.getId(), Long.MAX_VALUE));

		assertEquals(ShopErrorCode.NOT_FOUND, exception.getErrorCode());
		assertEquals(0, shopLikes.count());
	}

	private User user(String providerId) {
		return transactionTemplate.execute(status -> {
			User user = User.builder().authProvider(AuthProvider.KAKAO).providerUserId(providerId).build();
			entityManager.persist(user);
			return user;
		});
	}

	private Shop shop(User owner, ShopApprovalStatus approvalStatus) {
		return shops.saveAndFlush(Shop.builder().user(owner)
			.shopName("민서네일").shopAddress("서울특별시 마포구 양화로 123").approvalStatus(approvalStatus).build());
	}

	@TestConfiguration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@EntityScan(basePackageClasses = {Shop.class, User.class})
	@EnableJpaRepositories(basePackageClasses = ShopRepository.class)
	@Import({ShopLikeService.class, JpaAuditingConfig.class})
	static class PersistenceConfig {
	}
}
