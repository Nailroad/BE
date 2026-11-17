package nailroad.domain.design.repository;

import nailroad.domain.design.entity.Tag;
import nailroad.domain.design.entity.TagId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, TagId> {
    List<Tag> findByDesign_IdIn(Collection<Long> designIds);
}
