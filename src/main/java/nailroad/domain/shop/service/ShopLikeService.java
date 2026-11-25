package nailroad.domain.shop.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import nailroad.domain.shop.code.ShopErrorCode;
import nailroad.domain.shop.entity.Shop;
import nailroad.domain.shop.entity.ShopLike;
import nailroad.domain.shop.enums.ShopApprovalStatus;
import nailroad.domain.shop.repository.ShopLikeRepository;
import nailroad.domain.shop.repository.ShopRepository;
import nailroad.domain.user.entity.User;
import nailroad.global.exception.CustomException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class ShopLikeService {

	private final EntityManager entityManager;
	private final ShopRepository shopRepository;
	private final ShopLikeRepository shopLikeRepository;
	private final TransactionTemplate transactionTemplate;

	public void like(Long userId, Long shopId) {
		shopRepository.findById(shopId)
			.filter(shop -> shop.getApprovalStatus() == ShopApprovalStatus.APPROVED)
			.orElseThrow(() -> new CustomException(ShopErrorCode.NOT_FOUND));
		if (shopLikeRepository.existsByUser_IdAndShop_Id(userId, shopId)) {
			return;
		}
		try {
			// 동시 요청이 먼저 저장하면 unique 제약 위반으로 이 트랜잭션만 롤백되고, 결과는 찜한 상태로 같다.
			transactionTemplate.executeWithoutResult(status -> shopLikeRepository.save(ShopLike.builder()
				.user(entityManager.getReference(User.class, userId))
				.shop(entityManager.getReference(Shop.class, shopId))
				.build()));
		} catch (DataIntegrityViolationException alreadyLiked) {
			// 이미 다른 요청이 찜을 저장했으므로 성공으로 처리한다.
		}
	}
}
