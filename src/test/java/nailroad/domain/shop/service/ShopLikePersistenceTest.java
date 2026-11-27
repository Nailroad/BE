package nailroad.domain.shop.service;

import jakarta.persistence.EntityManager;
import java.util.List;
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

	// 찜한 샵의 찜을 해제하면 찜 행이 삭제되는지 확인한다.
	@Test
	void unlikeRemovesLike() {
		User customer = user("customer");
		Shop shop = shop(user("owner"), ShopApprovalStatus.APPROVED);
		service.like(customer.getId(), shop.getId());

		service.unlike(customer.getId(), shop.getId());

		assertFalse(shopLikes.existsByUser_IdAndShop_Id(customer.getId(), shop.getId()));
		assertEquals(0, shopLikes.count());
	}

	// 찜 해제가 요청한 사용자의 해당 샵 찜만 삭제하고 다른 찜은 남기는지 확인한다.
	@Test
	void unlikeKeepsOtherLikes() {
		User customer = user("customer");
		User another = user("another");
		User owner = user("owner");
		Shop shop = shop(owner, ShopApprovalStatus.APPROVED);
		Shop otherShop = shop(owner, ShopApprovalStatus.APPROVED);
		service.like(customer.getId(), shop.getId());
		service.like(customer.getId(), otherShop.getId());
		service.like(another.getId(), shop.getId());

		service.unlike(customer.getId(), shop.getId());

		assertFalse(shopLikes.existsByUser_IdAndShop_Id(customer.getId(), shop.getId()));
		assertTrue(shopLikes.existsByUser_IdAndShop_Id(customer.getId(), otherShop.getId()));
		assertTrue(shopLikes.existsByUser_IdAndShop_Id(another.getId(), shop.getId()));
	}

	// 찜하지 않은 샵의 찜을 해제해도 오류 없이 성공하는지 확인한다.
	@Test
	void unlikeWithoutLikeSucceeds() {
		User customer = user("customer");
		Shop shop = shop(user("owner"), ShopApprovalStatus.APPROVED);

		assertDoesNotThrow(() -> service.unlike(customer.getId(), shop.getId()));
	}

	// 찜한 뒤 승인 상태가 바뀌어 공개되지 않은 샵도 찜을 해제할 수 있는지 확인한다.
	@Test
	void unlikesShopThatIsNoLongerApproved() {
		User customer = user("customer");
		Shop shop = shop(user("owner"), ShopApprovalStatus.APPROVED);
		service.like(customer.getId(), shop.getId());
		transactionTemplate.executeWithoutResult(status -> entityManager
			.createQuery("update Shop s set s.approvalStatus = :status where s.id = :id")
			.setParameter("status", ShopApprovalStatus.REJECTED)
			.setParameter("id", shop.getId())
			.executeUpdate());

		service.unlike(customer.getId(), shop.getId());

		assertEquals(0, shopLikes.count());
	}

	// 샵 조회의 찜 여부가 요청한 사용자의 현재 찜 행만 반영하는지 확인한다.
	@Test
	void findsOnlyCurrentLikesOfUser() {
		User customer = user("customer");
		User another = user("another");
		User owner = user("owner");
		Shop liked = shop(owner, ShopApprovalStatus.APPROVED);
		Shop unliked = shop(owner, ShopApprovalStatus.APPROVED);
		Shop likedByAnother = shop(owner, ShopApprovalStatus.APPROVED);
		service.like(customer.getId(), liked.getId());
		service.like(customer.getId(), unliked.getId());
		service.unlike(customer.getId(), unliked.getId());
		service.like(another.getId(), likedByAnother.getId());

		List<Long> likedShopIds = shopLikes.findLikedShopIds(customer.getId(),
			List.of(liked.getId(), unliked.getId(), likedByAnother.getId()));

		assertEquals(List.of(liked.getId()), likedShopIds);
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
