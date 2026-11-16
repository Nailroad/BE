package nailroad.domain.design.repository;

import nailroad.domain.design.entity.Design;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DesignRepository extends JpaRepository<Design, Long> {
    List<Design> findByIdLessThanOrderByIdDesc(Long cursor, Pageable pageable);
}
