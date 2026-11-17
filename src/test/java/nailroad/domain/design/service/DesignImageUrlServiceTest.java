package nailroad.domain.design.service;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.junit.jupiter.api.Assertions.*;

class DesignImageUrlServiceTest {
    @Test
    void signsPrivateObjectKeyForBrowserGet() {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-access-key", "test-secret-key")))
                .build()) {
            DesignImageUrlService service = new DesignImageUrlService(presigner, "test-designs");

            String url = service.readUrl("designs/example.jpg");

            assertTrue(url.startsWith("https://test-designs.s3.ap-northeast-2.amazonaws.com/designs/example.jpg?"));
            assertTrue(url.contains("X-Amz-Expires=900"));
            assertTrue(url.contains("X-Amz-Signature="));
            assertNull(service.readUrl(null));
            assertNull(service.readUrl(" "));
        }
    }
}
