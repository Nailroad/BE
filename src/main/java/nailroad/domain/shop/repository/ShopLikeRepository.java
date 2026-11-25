package nailroad.domain.shop.repository;

import nailroad.domain.shop.entity.ShopLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopLikeRepository extends JpaRepository<ShopLike, Long> {

	boolean existsByUser_IdAndShop_Id(Long userId, Long shopId);
}
