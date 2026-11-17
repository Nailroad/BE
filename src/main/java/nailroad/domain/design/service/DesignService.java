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
import nailroad.domain.design.repository.DesignLikeRepository;
import nailroad.domain.design.repository.DesignRepository;
import nailroad.domain.design.repository.TagRepository;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesignService {
    private static final int SCAN_BATCH_SIZE = 200;

    private final DesignRepository designRepository;
    private final TagRepository tagRepository;
    private final DesignLikeRepository designLikeRepository;

    @Transactional(readOnly = true)
    public DesignDetailResponse getDetail(Long userId, Long designId) {
        if (userId == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }
        if (designId == null || designId < 1) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST);
        }

        Design design = designRepository.findById(designId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND));
        List<TagName> tagNames = tagRepository.findByDesign_IdIn(List.of(designId)).stream()
                .map(Tag::getTagName)
                .distinct()
                .sorted()
                .toList();
        boolean isLiked = !designLikeRepository
                .findByUser_IdAndDesign_IdInAndLikedTrue(userId, List.of(designId))
                .isEmpty();

        return new DesignDetailResponse(
                design.getId(),
                design.getShop().getId(),
                design.getName(),
                design.getDesignImageUrl(),
                design.getCategoryCodes(),
                design.getShapeCodes(),
                design.getColorCodes(),
                tagNames,
                isLiked
        );
    }

    @Transactional(readOnly = true)
    public DesignSearchResponse search(Long userId, String keyword, List<String> categoryCode,
                                       List<String> shapeCode, List<String> colorCode, List<String> tagName,
                                       String cursor, String size) {
        if (userId == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }
        int pageSize = parseSize(size);
        long scanCursor = parseCursor(cursor);
        Set<DesignCategoryCode> categories = parseFilter(categoryCode, DesignCategoryCode.class);
        Set<DesignShapeCode> shapes = parseFilter(shapeCode, DesignShapeCode.class);
        Set<DesignColorCode> colors = parseFilter(colorCode, DesignColorCode.class);
        Set<TagName> tags = parseFilter(tagName, TagName.class);
        String search = keyword == null ? "" : keyword.trim().toUpperCase(Locale.ROOT);

        List<Design> matches = new ArrayList<>(pageSize + 1);
        while (matches.size() <= pageSize) {
            List<Design> batch = designRepository.findByIdLessThanOrderByIdDesc(
                    scanCursor, PageRequest.of(0, SCAN_BATCH_SIZE));
            if (batch.isEmpty()) {
                break;
            }
            Map<Long, Set<TagName>> tagsByDesign = loadTags(batch, !tags.isEmpty() || !search.isEmpty());
            for (Design design : batch) {
                if (matches(design, tagsByDesign.getOrDefault(design.getId(), Set.of()),
                        categories, shapes, colors, tags, search)) {
                    matches.add(design);
                    if (matches.size() > pageSize) {
                        break;
                    }
                }
            }
            if (matches.size() > pageSize || batch.size() < SCAN_BATCH_SIZE) {
                break;
            }
            scanCursor = batch.getLast().getId();
        }

        boolean hasNext = matches.size() > pageSize;
        List<Design> page = matches.subList(0, Math.min(pageSize, matches.size()));
        Set<Long> likedIds = page.isEmpty() ? Set.of() :
                designLikeRepository.findByUser_IdAndDesign_IdInAndLikedTrue(
                        userId, page.stream().map(Design::getId).toList())
                        .stream().map(like -> like.getDesign().getId()).collect(Collectors.toSet());
        List<DesignSearchResponse.Item> items = page.stream()
                .map(design -> new DesignSearchResponse.Item(
                        design.getId(), design.getShop().getId(), design.getName(), design.getDesignImageUrl(),
                        design.getCategoryCodes(), design.getShapeCodes(), design.getColorCodes(),
                        likedIds.contains(design.getId())))
                .toList();
        Long nextCursor = hasNext ? page.getLast().getId() : null;
        return new DesignSearchResponse(items, pageSize, hasNext, nextCursor);
    }

    private Map<Long, Set<TagName>> loadTags(List<Design> designs, boolean needed) {
        if (!needed) {
            return Map.of();
        }
        Collection<Long> ids = designs.stream().map(Design::getId).toList();
        Map<Long, Set<TagName>> result = new HashMap<>();
        for (Tag tag : tagRepository.findByDesign_IdIn(ids)) {
            result.computeIfAbsent(tag.getDesign().getId(), ignored -> new HashSet<>()).add(tag.getTagName());
        }
        return result;
    }

    private boolean matches(Design design, Set<TagName> designTags,
                            Set<DesignCategoryCode> categories, Set<DesignShapeCode> shapes,
                            Set<DesignColorCode> colors, Set<TagName> tags, String search) {
        if (!matchesAny(design.getCategoryCodes(), categories)
                || !matchesAny(design.getShapeCodes(), shapes)
                || !matchesAny(design.getColorCodes(), colors)
                || !matchesAny(designTags, tags)) {
            return false;
        }
        return search.isEmpty()
                || (design.getName() != null && design.getName().toUpperCase(Locale.ROOT).contains(search))
                || designTags.stream().anyMatch(tag -> tag.name().contains(search));
    }

    private <E> boolean matchesAny(Collection<E> values, Set<E> filter) {
        return filter.isEmpty() || values != null && values.stream().anyMatch(filter::contains);
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
