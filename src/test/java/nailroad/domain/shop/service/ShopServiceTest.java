package nailroad.domain.shop.service;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import nailroad.domain.shop.dto.request.ShopRegistrationRequest;
import nailroad.domain.shop.entity.Shop;
import nailroad.domain.shop.enums.ShopApprovalStatus;
import nailroad.domain.shop.integration.AddressClient;
import nailroad.domain.shop.integration.AddressClient.Coordinates;
import nailroad.domain.shop.repository.ShopRepository;
import nailroad.domain.user.entity.User;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShopServiceTest {

	private final EntityManager entityManager = mock(EntityManager.class);
	private final ShopRepository shops = mock(ShopRepository.class);
	private final AddressClient addresses = mock(AddressClient.class);
	private final ShopService service = new ShopService(
		entityManager, shops, addresses, new TransactionTemplate(mock(PlatformTransactionManager.class)));
	private final User user = mock(User.class);
	private final ShopRegistrationRequest request = new ShopRegistrationRequest(
		"민서네일", "010-0000-0000", "서울특별시 마포구 양화로 123", "2층");

	@BeforeEach
	void setUp() {
		when(entityManager.getReference(User.class, 7L)).thenReturn(user);
		when(shops.save(any(Shop.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	// 조회한 좌표를 신청자와 연결된 신규 샵에 전달하는지 확인한다.
	@Test
	void registersShopWithResolvedCoordinates() {
		Coordinates point = new Coordinates(new BigDecimal("37.5567300"), new BigDecimal("126.9239600"));
		when(addresses.lookup(request.shopAddress())).thenReturn(Optional.of(point));

		service.register(7L, request);

		ArgumentCaptor<Shop> captor = ArgumentCaptor.forClass(Shop.class);
		verify(shops).save(captor.capture());
		assertSame(user, captor.getValue().getUser());
		assertEquals(point.latitude(), captor.getValue().getLatitude());
		assertEquals(point.longitude(), captor.getValue().getLongitude());
	}

	// 좌표 조회에 실패해도 PENDING 상태로 접수하는지 확인한다.
	@Test
	void acceptsApplicationWhenCoordinatesAreUnavailable() {
		when(addresses.lookup(request.shopAddress())).thenReturn(Optional.empty());

		var response = service.register(7L, request);

		ArgumentCaptor<Shop> captor = ArgumentCaptor.forClass(Shop.class);
		verify(shops).save(captor.capture());
		assertEquals(ShopApprovalStatus.PENDING, response.approvalStatus());
		assertNull(captor.getValue().getLatitude());
		assertNull(captor.getValue().getLongitude());
	}

	// 심사 중·승인된 동일 샵이 있으면 좌표 조회와 저장 전에 중복 오류를 반환하는지 확인한다.
	@Test
	void rejectsDuplicateApplication() {
		when(shops.existsByUser_IdAndShopNameAndShopAddressAndApprovalStatusIn(
			7L, request.shopName(), request.shopAddress(),
			Set.of(ShopApprovalStatus.PENDING, ShopApprovalStatus.APPROVED))).thenReturn(true);

		CustomException exception = assertThrows(CustomException.class, () -> service.register(7L, request));

		assertEquals(GlobalErrorCode.CONFLICT, exception.getErrorCode());
		verify(shops, never()).save(any());
		verifyNoInteractions(addresses);
	}
}
