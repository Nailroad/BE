package nailroad.domain.shop.service;

import java.util.Optional;
import nailroad.domain.image.service.CloudFrontImageUrlService;
import nailroad.domain.shop.code.DesignerErrorCode;
import nailroad.domain.shop.code.ShopErrorCode;
import nailroad.domain.shop.dto.request.DesignerRegistrationRequest;
import nailroad.domain.shop.dto.response.DesignerResponse;
import nailroad.domain.shop.entity.Designer;
import nailroad.domain.shop.entity.Shop;
import nailroad.domain.shop.repository.DesignerRepository;
import nailroad.domain.shop.repository.ShopRepository;
import nailroad.domain.user.entity.User;
import nailroad.global.exception.CustomException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DesignerServiceTest {

	private static final long OWNER_ID = 7L;
	private static final long SHOP_ID = 13L;
	private static final long DESIGNER_ID = 27L;

	private final ShopRepository shops = mock(ShopRepository.class);
	private final DesignerRepository designers = mock(DesignerRepository.class);
	private final CloudFrontImageUrlService imageUrls = mock(CloudFrontImageUrlService.class);
	private final DesignerService service = new DesignerService(shops, designers, imageUrls);
	private final User owner = mock(User.class);
	private final Shop shop = Shop.builder().user(owner).build();
	private final DesignerRegistrationRequest request = new DesignerRegistrationRequest(
		"민서", "designers/5d250e5f-17e5-45cb-86b5-d2e25aa92e09.jpg", "네일 아트 소개", "젤네일");

	// 소유한 샵에 디자이너 프로필을 저장하고 생성된 ID를 반환하는지 확인한다.
	@Test
	void registersDesignerForOwnedShop() {
		String imageUrl = "https://images.example.com/" + request.profileImageKey();
		when(owner.getId()).thenReturn(OWNER_ID);
		when(shops.findById(SHOP_ID)).thenReturn(Optional.of(shop));
		when(imageUrls.readUrl(request.profileImageKey())).thenReturn(imageUrl);
		when(designers.save(any(Designer.class))).thenAnswer(invocation -> {
			Designer designer = invocation.getArgument(0);
			ReflectionTestUtils.setField(designer, "id", DESIGNER_ID);
			return designer;
		});

		DesignerResponse response = service.register(OWNER_ID, SHOP_ID, request);

		ArgumentCaptor<Designer> captor = ArgumentCaptor.forClass(Designer.class);
		verify(designers).save(captor.capture());
		assertSame(shop, captor.getValue().getShop());
		assertEquals(new DesignerResponse(DESIGNER_ID, request.name(), imageUrl,
			request.description(), request.specialties()), response);
	}

	// 타인 소유 샵의 등록 요청을 저장 전에 거부하는지 확인한다.
	@Test
	void rejectsRegistrationForAnotherOwnersShop() {
		when(owner.getId()).thenReturn(OWNER_ID);
		when(shops.findById(SHOP_ID)).thenReturn(Optional.of(shop));

		CustomException exception = assertThrows(CustomException.class,
			() -> service.register(8L, SHOP_ID, request));

		assertEquals(DesignerErrorCode.NOT_SHOP_OWNER, exception.getErrorCode());
		verifyNoInteractions(designers, imageUrls);
	}

	// 샵이 없으면 찾을 수 없음 오류를 반환하는지 확인한다.
	@Test
	void rejectsRegistrationForMissingShop() {
		when(shops.findById(SHOP_ID)).thenReturn(Optional.empty());

		CustomException exception = assertThrows(CustomException.class,
			() -> service.register(OWNER_ID, SHOP_ID, request));

		assertEquals(ShopErrorCode.NOT_FOUND, exception.getErrorCode());
		verifyNoInteractions(designers, imageUrls);
	}
}
