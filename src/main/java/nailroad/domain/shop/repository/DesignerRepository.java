package nailroad.domain.shop.repository;

import java.util.Optional;
import nailroad.domain.shop.entity.Designer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DesignerRepository extends JpaRepository<Designer, Long> {

	Optional<Designer> findByIdAndShop_Id(Long designerId, Long shopId);

	@Modifying
	@Query("DELETE FROM Designer d WHERE d.id = :designerId AND d.shop.id = :shopId")
	int deleteForShop(@Param("designerId") Long designerId, @Param("shopId") Long shopId);
}
