package nailroad.domain.design.service;

import nailroad.domain.design.code.DesignErrorCode;
import nailroad.domain.design.dto.DesignDetailResponse;
import nailroad.domain.design.entity.Design;
import nailroad.domain.design.entity.Tag;
import nailroad.domain.design.enums.DesignCategoryCode;
import nailroad.domain.design.enums.DesignColorCode;
import nailroad.domain.design.enums.DesignShapeCode;
import nailroad.domain.design.enums.TagName;
import nailroad.domain.design.repository.DesignRepository;
import nailroad.domain.design.repository.DesignSearchRepository;
import nailroad.domain.design.repository.TagRepository;
import nailroad.domain.shop.entity.Shop;
import nailroad.global.exception.CustomException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DesignDetailServiceTest {
    private final DesignRepository designs = mock(DesignRepository.class);
    private final TagRepository tags = mock(TagRepository.class);
    private final DesignService service = new DesignService(designs, mock(DesignSearchRepository.class), tags);

    @Test
    void returnsDesignTags() {
        Design design = design();
        Tag wedding = tag(TagName.WEDDING_NAIL);
        Tag soft = tag(TagName.SOFT_NAIL);
        Tag duplicate = tag(TagName.WEDDING_NAIL);
        when(designs.findById(87L)).thenReturn(Optional.of(design));
        when(tags.findByDesign_IdIn(List.of(87L))).thenReturn(List.of(wedding, soft, duplicate));

        DesignDetailResponse response = service.getDetail(87L);

        assertEquals(87L, response.designId());
        assertEquals(5L, response.shopId());
        assertEquals("핑크 프렌치 네일", response.name());
        assertEquals("https://example.com/designs/87.jpg", response.designImageUrl());
        assertEquals(List.of(DesignCategoryCode.FRENCH), response.categoryCodes());
        assertEquals(List.of(DesignShapeCode.OVAL), response.shapeCodes());
        assertEquals(List.of(DesignColorCode.PINK), response.colorCodes());
        assertEquals(List.of(TagName.SOFT_NAIL, TagName.WEDDING_NAIL), response.tagNames());
        assertFalse(response.isLiked());
    }

    @Test
    void returnsEmptyTagsAndFalseLiked() {
        Design design = design();
        when(designs.findById(87L)).thenReturn(Optional.of(design));
        when(tags.findByDesign_IdIn(List.of(87L))).thenReturn(List.of());

        DesignDetailResponse response = service.getDetail(87L);

        assertEquals(List.of(), response.tagNames());
        assertFalse(response.isLiked());
    }

    @Test
    void rejectsInvalidId() {
        CustomException badRequest = assertThrows(CustomException.class,
                () -> service.getDetail(0L));

        assertEquals(DesignErrorCode.INVALID_DESIGN_ID, badRequest.getErrorCode());
        verifyNoInteractions(designs, tags);
    }

    @Test
    void returnsNotFoundForUnknownDesign() {
        when(designs.findById(87L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> service.getDetail(87L));

        assertEquals(DesignErrorCode.DESIGN_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(tags);
    }

    private Design design() {
        Design design = mock(Design.class);
        Shop shop = mock(Shop.class);
        when(design.getId()).thenReturn(87L);
        when(design.getShop()).thenReturn(shop);
        when(shop.getId()).thenReturn(5L);
        when(design.getName()).thenReturn("핑크 프렌치 네일");
        when(design.getDesignImageUrl()).thenReturn("https://example.com/designs/87.jpg");
        when(design.getCategoryCodes()).thenReturn(List.of(DesignCategoryCode.FRENCH));
        when(design.getShapeCodes()).thenReturn(List.of(DesignShapeCode.OVAL));
        when(design.getColorCodes()).thenReturn(List.of(DesignColorCode.PINK));
        return design;
    }

    private Tag tag(TagName name) {
        Tag tag = mock(Tag.class);
        when(tag.getTagName()).thenReturn(name);
        return tag;
    }
}
