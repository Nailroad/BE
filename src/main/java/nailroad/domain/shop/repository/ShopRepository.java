package nailroad.domain.shop.repository;

import java.util.Collection;
import nailroad.domain.shop.entity.Shop;
import nailroad.domain.shop.enums.ShopApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {

	boolean existsByUser_IdAndShopNameAndShopAddressAndApprovalStatusIn(
		Long userId, String shopName, String shopAddress, Collection<ShopApprovalStatus> statuses);
}
