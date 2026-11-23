package nailroad.domain.shop.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import nailroad.domain.shop.entity.Shop;
import nailroad.domain.shop.enums.ShopApprovalStatus;
import nailroad.domain.shop.enums.ShopStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopRepository extends JpaRepository<Shop, Long> {

	String LIST_COLUMNS = """
		s.shop_id AS "shopId", s.shop_name AS "shopName", s.shop_address AS "shopAddress",
		s.latitude, s.longitude, s.status, s.shop_image AS "imageKeys",
		""";
	String LATEST_LIST_COLUMNS = LIST_COLUMNS + """
		CAST(NULL AS double precision) AS "averageRating", CAST(0 AS bigint) AS "reviewCount"
		""";
	String PUBLIC_SHOPS = """
		FROM shop s
		WHERE s.approval_status = 'APPROVED'
		  AND jsonb_typeof(s.business_hour) = 'object'
		  AND s.business_hour <> CAST('{}' AS jsonb)
		  AND cardinality(s.shop_image) > 0
		""";
	String SHOP_NAME_FILTER = "AND (:namePattern = '%' OR s.shop_name ILIKE :namePattern ESCAPE '!')\n";
	String MAP_BOUNDS_FILTER = """
		AND s.latitude BETWEEN :south AND :north
		AND s.longitude BETWEEN :west AND :east
		""";
	String LATEST_PAGE = """
		AND s.shop_id <= :maxShopId
		ORDER BY s.shop_id DESC
		""";

	boolean existsByUser_IdAndShopNameAndShopAddressAndApprovalStatusIn(
		Long userId, String shopName, String shopAddress, Collection<ShopApprovalStatus> statuses);

	@Query(value = "SELECT " + LATEST_LIST_COLUMNS + PUBLIC_SHOPS + SHOP_NAME_FILTER + LATEST_PAGE,
		nativeQuery = true)
	Slice<ListRow> findPublicShops(@Param("maxShopId") long maxShopId,
		@Param("namePattern") String namePattern, Pageable pageable);

	@Query(value = "SELECT " + LATEST_LIST_COLUMNS + PUBLIC_SHOPS + SHOP_NAME_FILTER + MAP_BOUNDS_FILTER
		+ LATEST_PAGE, nativeQuery = true)
	Slice<ListRow> findLatestPublicMapShops(@Param("maxShopId") long maxShopId, @Param("namePattern") String namePattern,
		@Param("south") BigDecimal south, @Param("north") BigDecimal north,
		@Param("west") BigDecimal west, @Param("east") BigDecimal east, Pageable pageable);

	@Query(value = """
		WITH candidates AS (
			SELECT s.shop_id, s.latitude, s.longitude
		""" + PUBLIC_SHOPS + SHOP_NAME_FILTER + MAP_BOUNDS_FILTER + """
		), review_stats AS (
			SELECT r.shop_id, AVG(r.rating) AS average_rating, COUNT(*) AS review_count
			FROM candidates c JOIN shop_review r ON r.shop_id = c.shop_id
			WHERE :sort IN ('RATING', 'REVIEW_COUNT')
			GROUP BY r.shop_id
		), scored AS (
			SELECT c.shop_id, a.average_rating, COALESCE(a.review_count, 0) AS review_count,
				-- 구면 거리의 코사인 값이 클수록 지도 중심에 가깝습니다.
				CASE :sort WHEN 'DISTANCE' THEN
					SIN(RADIANS(c.latitude)) * SIN(RADIANS((:south + :north) / 2.0))
					+ COS(RADIANS(c.latitude)) * COS(RADIANS((:south + :north) / 2.0))
					  * COS(RADIANS(c.longitude - (:west + :east) / 2.0))
				WHEN 'RATING' THEN a.average_rating
				END AS sort_score
			FROM candidates c LEFT JOIN review_stats a ON a.shop_id = c.shop_id
		)
		SELECT
		""" + LIST_COLUMNS + """
			p.average_rating AS "averageRating", p.review_count AS "reviewCount"
		FROM scored p JOIN shop s ON s.shop_id = p.shop_id
		LEFT JOIN scored anchor ON anchor.shop_id = :cursorShopId
		WHERE :cursorShopId = 0
		   OR (anchor.shop_id IS NOT NULL AND CASE WHEN :sort = 'REVIEW_COUNT'
				THEN (p.review_count, p.shop_id) < (anchor.review_count, anchor.shop_id)
				ELSE (p.sort_score, p.shop_id) < (anchor.sort_score, anchor.shop_id)
					OR (p.sort_score IS NULL AND (anchor.sort_score IS NOT NULL OR p.shop_id < anchor.shop_id))
			END)
		ORDER BY p.sort_score DESC NULLS LAST,
			CASE WHEN :sort = 'REVIEW_COUNT' THEN p.review_count END DESC, p.shop_id DESC
		""", nativeQuery = true)
	Slice<ListRow> findPublicMapShops(@Param("namePattern") String namePattern, @Param("sort") String sort,
		@Param("south") BigDecimal south, @Param("north") BigDecimal north,
		@Param("west") BigDecimal west, @Param("east") BigDecimal east,
		@Param("cursorShopId") long cursorShopId, Pageable pageable);

	@Query(value = "SELECT s.* " + PUBLIC_SHOPS + "AND s.shop_id = :shopId", nativeQuery = true)
	Optional<Shop> findPublicShop(@Param("shopId") Long shopId);

	interface ListRow {
		Long getShopId();
		String getShopName();
		String getShopAddress();
		BigDecimal getLatitude();
		BigDecimal getLongitude();
		ShopStatus getStatus();
		String[] getImageKeys();
		Double getAverageRating();
		long getReviewCount();
	}
}
