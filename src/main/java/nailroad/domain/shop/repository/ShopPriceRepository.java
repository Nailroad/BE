package nailroad.domain.shop.repository;

import java.util.List;
import nailroad.domain.shop.entity.ShopPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopPriceRepository extends JpaRepository<ShopPrice, Long> {

	List<ShopPrice> findAllByShop_IdOrderByIdAsc(Long shopId);
}
