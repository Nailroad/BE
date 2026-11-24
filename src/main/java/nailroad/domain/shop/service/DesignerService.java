package nailroad.domain.shop.service;

import lombok.RequiredArgsConstructor;
import nailroad.domain.image.service.CloudFrontImageUrlService;
import nailroad.domain.shop.code.DesignerErrorCode;
import nailroad.domain.shop.code.ShopErrorCode;
import nailroad.domain.shop.dto.request.DesignerRegistrationRequest;
import nailroad.domain.shop.dto.response.DesignerResponse;
import nailroad.domain.shop.entity.Designer;
import nailroad.domain.shop.entity.Shop;
import nailroad.domain.shop.repository.DesignerRepository;
import nailroad.domain.shop.repository.ShopRepository;
import nailroad.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignerService {

	private final ShopRepository shopRepository;
	private final DesignerRepository designerRepository;
	private final CloudFrontImageUrlService imageUrlService;

	@Transactional
	public DesignerResponse register(Long userId, Long shopId, DesignerRegistrationRequest request) {
		Shop shop = shopRepository.findById(shopId)
			.orElseThrow(() -> new CustomException(ShopErrorCode.NOT_FOUND));
		if (!shop.getUser().getId().equals(userId)) {
			throw new CustomException(DesignerErrorCode.NOT_SHOP_OWNER);
		}

		Designer designer = designerRepository.save(Designer.builder()
			.shop(shop)
			.name(request.name())
			.profileImageUrl(imageUrlService.readUrl(request.profileImageKey()))
			.description(request.description())
			.specialties(request.specialties())
			.build());

		return new DesignerResponse(designer.getId(), designer.getName(), designer.getProfileImageUrl(),
			designer.getDescription(), designer.getSpecialties());
	}
}
