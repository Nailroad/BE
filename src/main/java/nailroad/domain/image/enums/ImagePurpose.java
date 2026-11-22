package nailroad.domain.image.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImagePurpose {

    SHOP("shops"),
    DESIGN("designs"),
    REVIEW("reviews"),
    DESIGNER("designers"),
    ;

    private final String keyPrefix;
}
