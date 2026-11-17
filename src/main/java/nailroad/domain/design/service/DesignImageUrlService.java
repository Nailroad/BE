package nailroad.domain.design.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@Service
public class DesignImageUrlService {
    private static final Duration URL_LIFETIME = Duration.ofMinutes(15);

    private final S3Presigner presigner;
    private final String bucket;

    public DesignImageUrlService(S3Presigner presigner, @Value("${app.s3.bucket}") String bucket) {
        this.presigner = presigner;
        this.bucket = bucket;
    }

    public String readUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(URL_LIFETIME)
                .getObjectRequest(objectRequest)
                .build();
        return presigner.presignGetObject(request).url().toString();
    }
}
