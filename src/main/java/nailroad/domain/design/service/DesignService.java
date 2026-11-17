package nailroad.domain.design.service;

import lombok.RequiredArgsConstructor;
import nailroad.domain.design.code.DesignErrorCode;
import nailroad.domain.design.dto.DesignDetailResponse;
import nailroad.domain.design.dto.DesignSearchResponse;
import nailroad.domain.design.entity.Design;
import nailroad.domain.design.entity.Tag;
import nailroad.domain.design.enums.DesignCategoryCode;
import nailroad.domain.design.enums.DesignColorCode;
import nailroad.domain.design.enums.DesignShapeCode;
import nailroad.domain.design.enums.TagName;
import nailroad.domain.design.repository.DesignRepository;
import nailroad.domain.design.repository.DesignSearchRepository;
import nailroad.domain.design.repository.TagRepository;
import nailroad.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DesignService {
    private final DesignRepository designRepository;
    private final DesignSearchRepository designSearchRepository;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public DesignDetailResponse getDetail(Long designId) {
        if (designId == null || designId < 1) {
            throw new CustomException(DesignErrorCode.INVALID_DESIGN_ID);
        }

        Design design = designRepository.findById(designId)
                .orElseThrow(() -> new CustomException(DesignErrorCode.DESIGN_NOT_FOUND));
        List<TagName> tagNames = tagRepository.findByDesign_IdIn(List.of(designId)).stream()
                .map(Tag::getTagName)
                .distinct()
                .sorted()
                .toList();
        return new DesignDetailResponse(
                design.getId(),
                design.getShop().getId(),
                design.getName(),
                design.getDesignImageUrl(),
                design.getCategoryCodes(),
                design.getShapeCodes(),
                design.getColorCodes(),
                tagNames,
                false
        );
    }

    @Transactional(readOnly = true)
    public DesignSearchResponse search(String keyword, List<String> categoryCode,
                                       List<String> shapeCode, List<String> colorCode, List<String> tagName,
                                       String cursor, String size) {
        int pageSize = parseSize(size);
        long searchCursor = parseCursor(cursor);
        Set<DesignCategoryCode> categories = parseFilter(categoryCode, DesignCategoryCode.class);
        Set<DesignShapeCode> shapes = parseFilter(shapeCode, DesignShapeCode.class);
        Set<DesignColorCode> colors = parseFilter(colorCode, DesignColorCode.class);
        Set<TagName> tags = parseFilter(tagName, TagName.class);
        String search = keyword == null ? "" : keyword.trim().toUpperCase(Locale.ROOT);

        List<Long> matchingIds = designSearchRepository.findMatchingIds(
                searchCursor, pageSize + 1, search, categories, shapes, colors, tags);
        boolean hasNext = matchingIds.size() > pageSize;
        List<Long> pageIds = matchingIds.subList(0, Math.min(pageSize, matchingIds.size()));
        Map<Long, Design> designsById = new HashMap<>();
        if (!pageIds.isEmpty()) {
            designRepository.findByIdIn(pageIds).forEach(design -> designsById.put(design.getId(), design));
        }
        List<Design> page = pageIds.stream().map(designsById::get).toList();
        List<DesignSearchResponse.Item> items = page.stream()
                .map(design -> new DesignSearchResponse.Item(
                        design.getId(), design.getShop().getId(), design.getName(), design.getDesignImageUrl(),
                        design.getCategoryCodes(), design.getShapeCodes(), design.getColorCodes(),
                        false))
                .toList();
        Long nextCursor = hasNext ? pageIds.getLast() : null;
        return new DesignSearchResponse(items, pageSize, hasNext, nextCursor);
    }

    private <E extends Enum<E>> Set<E> parseFilter(List<String> values, Class<E> type) {
        if (values == null) {
            return Set.of();
        }
        Set<E> result = new HashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            try {
                result.add(Enum.valueOf(type, value.trim()));
            } catch (IllegalArgumentException exception) {
                throw new CustomException(DesignErrorCode.INVALID_FILTER);
            }
        }
        return result;
    }

    private long parseCursor(String cursor) {
        if (cursor == null) {
            return Long.MAX_VALUE;
        }
        try {
            long value = Long.parseLong(cursor);
            if (value > 0) {
                return value;
            }
        } catch (NumberFormatException ignored) {
        }
        throw new CustomException(DesignErrorCode.INVALID_CURSOR);
    }

    private int parseSize(String size) {
        try {
            int value = Integer.parseInt(size);
            if (value >= 1 && value <= 50) {
                return value;
            }
        } catch (NumberFormatException ignored) {
        }
        throw new CustomException(DesignErrorCode.INVALID_PAGE_SIZE);
    }
}
