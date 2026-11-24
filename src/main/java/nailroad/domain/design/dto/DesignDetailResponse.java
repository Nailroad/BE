package nailroad.domain.design.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import nailroad.domain.design.enums.DesignCategoryCode;
import nailroad.domain.design.enums.DesignColorCode;
import nailroad.domain.design.enums.DesignShapeCode;
import nailroad.domain.design.enums.TagName;

public record DesignDetailResponse(
        Long designId,
        Long shopId,
        String shopName,
        String shopAddress,
        String name,
        String designImageUrl,
        @JsonProperty("image_width") Integer imageWidth,
        @JsonProperty("image_height") Integer imageHeight,
        List<DesignCategoryCode> categoryCodes,
        List<DesignShapeCode> shapeCodes,
        List<DesignColorCode> colorCodes,
        List<TagName> tagNames,
        boolean isLiked
) {
}
