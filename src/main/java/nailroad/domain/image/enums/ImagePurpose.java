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
    /** 매칭(직접 업로드) 견적 요청 이미지. 키는 estimates/{userId}/... 로 발급한다. */
    ESTIMATE("estimates"),
    ;

    private final String keyPrefix;
}
