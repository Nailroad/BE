package nailroad.domain.design.repository;

import nailroad.domain.design.entity.Design;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DesignRepository extends JpaRepository<Design, Long> {
    @Override
    @EntityGraph(attributePaths = "shop")
    Optional<Design> findById(Long id);

    @EntityGraph(attributePaths = "shop")
    List<Design> findByIdIn(Collection<Long> ids);
}
