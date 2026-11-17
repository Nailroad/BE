package nailroad.domain.design.repository;

import nailroad.domain.design.entity.Design;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface DesignRepository extends JpaRepository<Design, Long> {
    @EntityGraph(attributePaths = "shop")
    List<Design> findByIdIn(Collection<Long> ids);
}
