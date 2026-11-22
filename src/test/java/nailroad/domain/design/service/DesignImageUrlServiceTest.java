package nailroad.domain.design.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DesignImageUrlServiceTest {
    private final DesignImageUrlService service = new DesignImageUrlService("https://images.example.com/");

    @Test
    void returnsCloudFrontUrlForExistingDesign() {
        assertEquals("https://images.example.com/designs/001.jpg", service.readUrl("designs/001.jpg"));
    }

    @Test
    void encodesSpecialCharactersInTheObjectKey() {
        assertEquals("https://images.example.com/designs/23/%ED%8C%8C%EC%B8%A0%20%2B%20%23%3F%25.png",
                service.readUrl("designs/23/파츠 + #?%.png"));
    }

    @Test
    void returnsNullWhenTheDesignHasNoImage() {
        assertNull(service.readUrl(null));
        assertNull(service.readUrl(" "));
    }

    @Test
    void rejectsAnInvalidCloudFrontBaseUrl() {
        assertThrows(IllegalArgumentException.class, () -> new DesignImageUrlService(""));
        assertThrows(IllegalArgumentException.class, () -> new DesignImageUrlService("http://images.example.com"));
        assertThrows(IllegalArgumentException.class, () -> new DesignImageUrlService("https://images.example.com/designs"));
    }
}
