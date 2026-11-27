package nailroad.domain.image.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CloudFrontImageUrlServiceTest {
    private final CloudFrontImageUrlService service = new CloudFrontImageUrlService("https://images.example.com/");

    @ParameterizedTest
    @ValueSource(strings = {"designs/001.jpg", "shops/shop.jpg", "reviews/review.jpg", "designers/profile.jpg"})
    void returnsCloudFrontUrlForImageKey(String imageKey) {
        assertEquals("https://images.example.com/" + imageKey, service.readUrl(imageKey));
    }

    @Test
    void encodesSpecialCharactersInTheObjectKey() {
        assertEquals("https://images.example.com/designs/23/%ED%8C%8C%EC%B8%A0%20%2B%20%23%3F%25.png",
                service.readUrl("designs/23/파츠 + #?%.png"));
    }

    @Test
    void returnsNullWhenTheImageKeyIsMissing() {
        assertNull(service.readUrl(null));
        assertNull(service.readUrl(" "));
    }

    @Test
    void rejectsAnInvalidCloudFrontBaseUrl() {
        assertThrows(IllegalArgumentException.class, () -> new CloudFrontImageUrlService(""));
        assertThrows(IllegalArgumentException.class, () -> new CloudFrontImageUrlService("http://images.example.com"));
        assertThrows(IllegalArgumentException.class, () -> new CloudFrontImageUrlService("https://images.example.com/designs"));
    }
}
