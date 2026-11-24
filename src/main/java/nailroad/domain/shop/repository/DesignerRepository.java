package nailroad.domain.shop.repository;

import java.util.Optional;
import nailroad.domain.shop.entity.Designer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignerRepository extends JpaRepository<Designer, Long> {

	Optional<Designer> findByIdAndShop_Id(Long designerId, Long shopId);
}
