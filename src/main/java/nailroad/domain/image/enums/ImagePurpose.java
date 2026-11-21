package nailroad.domain.image.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImagePurpose {

    SHOP("shops"),
    DESIGN("designs"),
    REVIEW("reviews");

    private final String keyPrefix;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ImagePurpose fromValue(String value) {
        return ImagePurpose.valueOf(value);
    }
}
