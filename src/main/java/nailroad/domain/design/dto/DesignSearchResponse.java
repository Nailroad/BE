package nailroad.domain.design.dto;

import nailroad.domain.design.enums.DesignCategoryCode;
import nailroad.domain.design.enums.DesignColorCode;
import nailroad.domain.design.enums.DesignShapeCode;
import java.util.List;

public record DesignSearchResponse(List<Item> items, int size, boolean hasNext, Long nextCursor, long totalCount) {
    public record Item(Long designId, Long shopId, String name, String designImageUrl,
                       List<DesignCategoryCode> categoryCodes, List<DesignShapeCode> shapeCodes,
                       List<DesignColorCode> colorCodes, boolean isLiked) {
    }
}
