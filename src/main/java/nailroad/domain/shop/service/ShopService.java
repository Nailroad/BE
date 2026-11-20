package nailroad.domain.shop.service;

import jakarta.persistence.EntityManager;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import nailroad.domain.shop.dto.request.ShopRegistrationRequest;
import nailroad.domain.shop.dto.response.ShopRegistrationResponse;
import nailroad.domain.shop.entity.Shop;
import nailroad.domain.shop.enums.ShopApprovalStatus;
import nailroad.domain.shop.enums.ShopStatus;
import nailroad.domain.shop.repository.ShopRepository;
import nailroad.domain.user.entity.User;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopService {

	private static final Set<ShopApprovalStatus> DUPLICATE_REGISTRATION_STATUSES =
		Set.of(ShopApprovalStatus.PENDING, ShopApprovalStatus.APPROVED);

	private final EntityManager entityManager;
	private final ShopRepository shopRepository;

	@Transactional
	public ShopRegistrationResponse register(Long userId, ShopRegistrationRequest request) {
		if (userId == null || userId < 1) {
			throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
		}

		checkDuplicate(userId, request);
		User user = entityManager.getReference(User.class, userId);
		Shop shop = shopRepository.save(Shop.builder()
			.user(user)
			.shopName(request.shopName())
			.shopPhone(request.shopPhone())
			.shopAddress(request.shopAddress())
			.detailAddress(request.detailAddress())
			.approvalStatus(ShopApprovalStatus.PENDING)
			.status(ShopStatus.PAUSED)
			.build());
		return new ShopRegistrationResponse(shop.getId(), shop.getApprovalStatus());
	}

	private void checkDuplicate(Long userId, ShopRegistrationRequest request) {
		if (shopRepository.existsByUser_IdAndShopNameAndShopAddressAndApprovalStatusIn(
			userId, request.shopName(), request.shopAddress(), DUPLICATE_REGISTRATION_STATUSES)) {
			throw new CustomException(GlobalErrorCode.CONFLICT);
		}
	}
}
