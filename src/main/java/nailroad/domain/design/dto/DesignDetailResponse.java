package nailroad.domain.design.dto;

import java.util.List;
import nailroad.domain.design.enums.DesignCategoryCode;
import nailroad.domain.design.enums.DesignColorCode;
import nailroad.domain.design.enums.DesignShapeCode;
import nailroad.domain.design.enums.TagName;

public record DesignDetailResponse(
        Long designId,
        Long shopId,
        String name,
        String designImageUrl,
        List<DesignCategoryCode> categoryCodes,
        List<DesignShapeCode> shapeCodes,
        List<DesignColorCode> colorCodes,
        List<TagName> tagNames,
        boolean isLiked
) {
}
