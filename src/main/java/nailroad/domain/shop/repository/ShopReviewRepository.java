package nailroad.domain.shop.repository;

import java.util.Collection;
import java.util.List;
import nailroad.domain.shop.entity.ShopReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopReviewRepository extends JpaRepository<ShopReview, Long> {

	@Query("""
		SELECT review.shop.id AS shopId, AVG(review.rating) AS averageRating, COUNT(review) AS reviewCount
		FROM ShopReview review
		WHERE review.shop.id IN :shopIds
		GROUP BY review.shop.id
		""")
	List<Summary> summarizeByShopIds(@Param("shopIds") Collection<Long> shopIds);

	interface Summary {
		Long getShopId();

		Double getAverageRating();

		long getReviewCount();
	}
}
