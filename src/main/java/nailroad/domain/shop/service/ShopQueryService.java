package nailroad.domain.shop.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nailroad.domain.image.service.CloudFrontImageUrlService;
import nailroad.domain.shop.code.ShopErrorCode;
import nailroad.domain.shop.dto.request.ShopQueryRequest;
import nailroad.domain.shop.dto.request.ShopQueryRequest.Sort;
import nailroad.domain.shop.dto.response.ShopQueryResponse.DetailResponse;
import nailroad.domain.shop.dto.response.ShopQueryResponse.ListResponse;
import nailroad.domain.shop.entity.Shop;
import nailroad.domain.shop.repository.ShopLikeRepository;
import nailroad.domain.shop.repository.ShopPriceRepository;
import nailroad.domain.shop.repository.ShopRepository;
import nailroad.domain.shop.repository.ShopRepository.ListRow;
import nailroad.domain.shop.repository.ShopReviewRepository;
import nailroad.domain.shop.repository.ShopReviewRepository.Summary;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopQueryService {

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 100;

	private final ShopRepository shopRepository;
	private final ShopPriceRepository shopPriceRepository;
	private final ShopReviewRepository shopReviewRepository;
	private final ShopLikeRepository shopLikeRepository;
	private final CloudFrontImageUrlService imageUrlService;

	@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
	public ListResponse getList(Long userId, ShopQueryRequest request) {
		int size = Objects.requireNonNullElse(request.size(), DEFAULT_PAGE_SIZE);
		if (size < 1 || size > MAX_PAGE_SIZE || (request.cursor() != null && request.cursor() < 1)) {
			throw new CustomException(GlobalErrorCode.BAD_REQUEST);
		}
		Sort sort = Objects.requireNonNullElse(request.sort(), request.hasMapBounds() ? Sort.DISTANCE : Sort.LATEST);
		Slice<ListRow> shops = findPage(request, sort, PageRequest.ofSize(size));
		List<Long> shopIds = shops.stream().map(ListRow::getShopId).toList();
		Map<Long, Summary> reviewsByShop = findPageReviewSummaries(shopIds, sort);
		Set<Long> likedShopIds = findLikedShopIds(userId, shopIds);
		List<ListResponse.Item> items = shops.stream()
			.map(shop -> toListItem(shop, reviewsByShop.get(shop.getShopId()), likedShopIds.contains(shop.getShopId())))
			.toList();
		Long nextCursor = shops.hasNext() ? items.getLast().shopId() : null;
		return new ListResponse(items, size, shops.hasNext(), nextCursor);
	}

	public DetailResponse getDetail(Long userId, Long shopId) {
		if (shopId < 1) {
			throw new CustomException(GlobalErrorCode.BAD_REQUEST);
		}
		Shop shop = shopRepository.findPublicShop(shopId)
			.orElseThrow(() -> new CustomException(ShopErrorCode.NOT_FOUND));
		List<DetailResponse.Price> prices = shopPriceRepository.findAllByShop_IdOrderByIdAsc(shopId).stream()
			.map(price -> new DetailResponse.Price(price.getId(), price.getServiceCategory(),
				price.getServiceName(), price.getServicePrice()))
			.toList();
		Summary reviewSummary = shopReviewRepository.summarizeByShopIds(List.of(shopId)).stream()
			.findFirst().orElse(null);
		boolean isLiked = findLikedShopIds(userId, List.of(shopId)).contains(shopId);
		return new DetailResponse(shop.getId(), shop.getShopName(), shop.getShopPhone(), shop.getShopAddress(),
			shop.getDetailAddress(), shop.getLatitude(), shop.getLongitude(), shop.getLocation(), shop.getStatus(),
			shop.getBusinessHour(), shop.getFacility(), readImageUrls(shop.getShopImage()), prices,
			reviewSummary == null ? null : reviewSummary.getAverageRating(),
			reviewSummary == null ? 0 : reviewSummary.getReviewCount(), isLiked);
	}

	private Slice<ListRow> findPage(ShopQueryRequest request, Sort sort, PageRequest page) {
		String namePattern = namePattern(request.keyword());
		long cursor = Objects.requireNonNullElse(request.cursor(), 0L);
		long maxShopId = cursor == 0 ? Long.MAX_VALUE : cursor - 1;
		if (sort == Sort.LATEST && !request.hasMapBounds()) {
			return shopRepository.findPublicShops(maxShopId, namePattern, page);
		}
		if (!request.hasValidMapBounds()) {
			throw new CustomException(GlobalErrorCode.BAD_REQUEST);
		}
		if (sort == Sort.LATEST) {
			return shopRepository.findLatestPublicMapShops(maxShopId, namePattern,
				request.south(), request.north(), request.west(), request.east(), page);
		}
		return shopRepository.findPublicMapShops(namePattern, sort.name(),
			request.south(), request.north(), request.west(), request.east(), cursor, page);
	}

	private Map<Long, Summary> findPageReviewSummaries(List<Long> shopIds, Sort sort) {
		if (shopIds.isEmpty() || sort == Sort.RATING || sort == Sort.REVIEW_COUNT) {
			return Map.of();
		}
		return shopReviewRepository.summarizeByShopIds(shopIds).stream()
			.collect(Collectors.toMap(Summary::getShopId, summary -> summary));
	}

	private Set<Long> findLikedShopIds(Long userId, List<Long> shopIds) {
		if (userId == null || shopIds.isEmpty()) {
			return Set.of();
		}
		return Set.copyOf(shopLikeRepository.findLikedShopIds(userId, shopIds));
	}

	private ListResponse.Item toListItem(ListRow shop, Summary reviewSummary, boolean isLiked) {
		return new ListResponse.Item(shop.getShopId(), shop.getShopName(), shop.getShopAddress(),
			shop.getLatitude(), shop.getLongitude(), shop.getStatus(), readImageUrls(Arrays.asList(shop.getImageKeys())),
			reviewSummary == null ? shop.getAverageRating() : reviewSummary.getAverageRating(),
			reviewSummary == null ? shop.getReviewCount() : reviewSummary.getReviewCount(), isLiked);
	}

	private String namePattern(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return "%";
		}
		return "%" + keyword.strip().replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
	}

	private List<String> readImageUrls(List<String> imageKeys) {
		return imageKeys.stream().map(imageUrlService::readUrl).toList();
	}
}
