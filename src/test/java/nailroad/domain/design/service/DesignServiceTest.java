package nailroad.domain.design.service;

import nailroad.domain.design.dto.DesignSearchResponse;
import nailroad.domain.design.entity.Design;
import nailroad.domain.design.enums.DesignCategoryCode;
import nailroad.domain.design.enums.DesignColorCode;
import nailroad.domain.design.enums.DesignShapeCode;
import nailroad.domain.design.enums.TagName;
import nailroad.domain.design.repository.DesignRepository;
import nailroad.domain.design.repository.DesignSearchRepository;
import nailroad.domain.design.repository.TagRepository;
import nailroad.domain.shop.entity.Shop;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DesignServiceTest {
    private final DesignRepository designs = mock(DesignRepository.class);
    private final DesignSearchRepository searchRepository = mock(DesignSearchRepository.class);
    private final TagRepository tags = mock(TagRepository.class);
    private final DesignImageUrlService images = mock(DesignImageUrlService.class);
    private final DesignService service = new DesignService(designs, searchRepository, tags, images);

    @Test
    void searchReturnsItemsWithFalseLiked() {
        Design design = design(20L, "화이트 프렌치", DesignColorCode.WHITE);
        when(searchRepository.findMatchingIds(Long.MAX_VALUE, 21, "", Set.of(), Set.of(), Set.of(), Set.of()))
                .thenReturn(List.of(20L));
        when(searchRepository.countMatching("", Set.of(), Set.of(), Set.of(), Set.of()))
                .thenReturn(1L);
        when(designs.findByIdIn(List.of(20L))).thenReturn(List.of(design));
        when(images.readUrl("designs/20.jpg")).thenReturn("https://example.com/presigned-20.jpg");

        DesignSearchResponse result = service.search(null, null, null, null, null, null, "20");

        assertEquals(1, result.items().size());
        assertEquals("https://example.com/presigned-20.jpg", result.items().getFirst().designImageUrl());
        assertEquals(1200, result.items().getFirst().imageWidth());
        assertEquals(800, result.items().getFirst().imageHeight());
        verify(images).readUrl("designs/20.jpg");
        assertFalse(result.items().getFirst().isLiked());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
        assertEquals(1L, result.totalCount());
    }

    @Test
    void keywordAndFiltersArePassedToDatabaseAndPageOrderIsPreserved() {
        Design first = design(80L, "봄 네일", DesignColorCode.PINK);
        when(searchRepository.findMatchingIds(91L, 2, "SPRING", Set.of(DesignCategoryCode.FRENCH),
                Set.of(DesignShapeCode.OVAL), Set.of(DesignColorCode.PINK), Set.of(TagName.SPRING_WARM_NAIL)))
                .thenReturn(List.of(80L, 70L));
        when(searchRepository.countMatching("SPRING", Set.of(DesignCategoryCode.FRENCH),
                Set.of(DesignShapeCode.OVAL), Set.of(DesignColorCode.PINK), Set.of(TagName.SPRING_WARM_NAIL)))
                .thenReturn(6L);
        when(designs.findByIdIn(List.of(80L))).thenReturn(List.of(first));

        DesignSearchResponse result = service.search("spring", List.of("FRENCH"), List.of("OVAL"),
                List.of("PINK"), List.of("SPRING_WARM_NAIL"), "91", "1");

        assertEquals(List.of(80L), result.items().stream().map(DesignSearchResponse.Item::designId).toList());
        assertTrue(result.hasNext());
        assertEquals(80L, result.nextCursor());
        assertEquals(1, result.size());
        assertEquals(6L, result.totalCount());
        verifyNoInteractions(tags);
    }

    @Test
    void matchingIdsCanBeLoadedInDifferentOrder() {
        Design first = design(80L, "첫째", DesignColorCode.PINK);
        Design second = design(70L, "둘째", DesignColorCode.PINK);
        when(searchRepository.findMatchingIds(Long.MAX_VALUE, 21, "", Set.of(), Set.of(), Set.of(), Set.of()))
                .thenReturn(List.of(80L, 70L));
        when(designs.findByIdIn(List.of(80L, 70L))).thenReturn(List.of(second, first));

        DesignSearchResponse result = service.search(null, null, null, null, null, null, "20");

        assertEquals(List.of(80L, 70L), result.items().stream().map(DesignSearchResponse.Item::designId).toList());
    }

    @Test
    void emptySearchDoesNotLoadDesigns() {
        when(searchRepository.findMatchingIds(Long.MAX_VALUE, 21, "", Set.of(), Set.of(), Set.of(), Set.of()))
                .thenReturn(List.of());

        DesignSearchResponse result = service.search(null, null, null, null, null, null, "20");

        assertTrue(result.items().isEmpty());
        verifyNoInteractions(designs, tags, images);
    }

    private Design design(Long id, String name, DesignColorCode color) {
        Design design = mock(Design.class);
        Shop shop = mock(Shop.class);
        when(design.getId()).thenReturn(id);
        when(design.getName()).thenReturn(name);
        when(design.getShop()).thenReturn(shop);
        when(shop.getId()).thenReturn(5L);
        when(design.getDesignImageUrl()).thenReturn("designs/" + id + ".jpg");
        when(design.getImageWidth()).thenReturn(1200);
        when(design.getImageHeight()).thenReturn(800);
        when(design.getCategoryCodes()).thenReturn(List.of(DesignCategoryCode.FRENCH));
        when(design.getShapeCodes()).thenReturn(List.of(DesignShapeCode.OVAL));
        when(design.getColorCodes()).thenReturn(List.of(color));
        return design;
    }
}
