package nailroad.domain.design.service;

import nailroad.domain.design.dto.DesignSearchResponse;
import nailroad.domain.design.entity.Design;
import nailroad.domain.design.entity.Tag;
import nailroad.domain.design.enums.DesignCategoryCode;
import nailroad.domain.design.enums.DesignColorCode;
import nailroad.domain.design.enums.DesignShapeCode;
import nailroad.domain.design.enums.TagName;
import nailroad.domain.design.repository.DesignRepository;
import nailroad.domain.design.repository.TagRepository;
import nailroad.domain.shop.entity.Shop;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DesignServiceTest {
    private final DesignRepository designs = mock(DesignRepository.class);
    private final TagRepository tags = mock(TagRepository.class);
    private final DesignService service = new DesignService(designs, tags);

    @Test
    void searchReturnsItemsWithFalseLiked() {
        Design design = design(20L, "화이트 프렌치", DesignColorCode.WHITE);
        when(designs.findByIdLessThanOrderByIdDesc(eq(Long.MAX_VALUE), any(Pageable.class)))
                .thenReturn(List.of(design));

        DesignSearchResponse result = service.search(
                null, null, null, null, null, null, "20");

        assertEquals(1, result.items().size());
        assertFalse(result.items().getFirst().isLiked());
    }

    @Test
    void keywordMatchesTagNameAndPaginatesAfterFiltering() {
        Design first = design(90L, "하트", DesignColorCode.RED);
        Design second = design(80L, "봄 네일", DesignColorCode.PINK);
        Design third = design(70L, "꽃", DesignColorCode.PINK);
        Tag firstTag = tag(first, TagName.WINTER_NAIL);
        Tag secondTag = tag(second, TagName.SPRING_WARM_NAIL);
        Tag thirdTag = tag(third, TagName.SPRING_WARM_NAIL);
        when(designs.findByIdLessThanOrderByIdDesc(eq(Long.MAX_VALUE), any(Pageable.class)))
                .thenReturn(List.of(first, second, third));
        when(tags.findByDesign_IdIn(anyCollection())).thenReturn(List.of(firstTag, secondTag, thirdTag));

        DesignSearchResponse result = service.search(
                "SPRING", null, null, List.of("PINK"), null, null, "1");

        assertEquals(List.of(80L), result.items().stream().map(DesignSearchResponse.Item::designId).toList());
        assertTrue(result.hasNext());
        assertEquals(80L, result.nextCursor());
        assertEquals(1, result.size());
    }

    @Test
    void tagFilterUsesEnumAndKeywordCanMatchDesignName() {
        Design design = design(20L, "화이트 프렌치", DesignColorCode.WHITE);
        Tag weddingTag = tag(design, TagName.WEDDING_NAIL);
        when(designs.findByIdLessThanOrderByIdDesc(eq(Long.MAX_VALUE), any(Pageable.class)))
                .thenReturn(List.of(design));
        when(tags.findByDesign_IdIn(anyCollection())).thenReturn(List.of(weddingTag));

        DesignSearchResponse result = service.search(
                "프렌치", List.of("FRENCH"), null, null,
                List.of("WEDDING_NAIL", "PARTY_NAIL"), null, "20");

        assertEquals(1, result.items().size());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
    }

    private Design design(Long id, String name, DesignColorCode color) {
        Design design = mock(Design.class);
        Shop shop = mock(Shop.class);
        when(design.getId()).thenReturn(id);
        when(design.getName()).thenReturn(name);
        when(design.getShop()).thenReturn(shop);
        when(shop.getId()).thenReturn(5L);
        when(design.getCategoryCodes()).thenReturn(List.of(DesignCategoryCode.FRENCH));
        when(design.getShapeCodes()).thenReturn(List.of(DesignShapeCode.OVAL));
        when(design.getColorCodes()).thenReturn(List.of(color));
        return design;
    }

    private Tag tag(Design design, TagName name) {
        Tag tag = mock(Tag.class);
        when(tag.getDesign()).thenReturn(design);
        when(tag.getTagName()).thenReturn(name);
        return tag;
    }
}
