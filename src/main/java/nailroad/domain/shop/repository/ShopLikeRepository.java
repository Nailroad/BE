package nailroad.domain.shop.repository;

import nailroad.domain.shop.entity.ShopLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopLikeRepository extends JpaRepository<ShopLike, Long> {

	boolean existsByUser_IdAndShop_Id(Long userId, Long shopId);

	@Modifying
	@Query("DELETE FROM ShopLike shopLike WHERE shopLike.user.id = :userId AND shopLike.shop.id = :shopId")
	void deleteByUserIdAndShopId(@Param("userId") Long userId, @Param("shopId") Long shopId);
}
