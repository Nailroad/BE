package nailroad.domain.design.repository;

import nailroad.domain.design.entity.DesignLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface DesignLikeRepository extends JpaRepository<DesignLike, Long> {
    List<DesignLike> findByUser_IdAndDesign_IdInAndLikedTrue(Long userId, Collection<Long> designIds);
}
