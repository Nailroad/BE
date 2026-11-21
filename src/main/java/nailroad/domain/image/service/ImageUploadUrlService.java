package nailroad.domain.image.service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import nailroad.domain.image.dto.ImageUploadUrlRequest;
import nailroad.domain.image.dto.ImageUploadUrlResponse;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class ImageUploadUrlService {

    private static final int EXPIRES_IN_SECONDS = 300;
    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final S3Presigner presigner;
    private final String bucket;

    public ImageUploadUrlService(S3Presigner presigner, @Value("${app.s3.bucket}") String bucket) {
        this.presigner = presigner;
        this.bucket = bucket;
    }

    public ImageUploadUrlResponse issue(ImageUploadUrlRequest request) {
        String extension = IMAGE_EXTENSIONS.get(request.contentType());
        if (extension == null) {
            throw new CustomException(GlobalErrorCode.VALIDATION_FAILED);
        }

        String imageKey = request.purpose().getKeyPrefix() + "/" + UUID.randomUUID() + "." + extension;
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(imageKey)
                .contentType(request.contentType())
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(EXPIRES_IN_SECONDS))
                .putObjectRequest(objectRequest)
                .build();

        String uploadUrl = presigner.presignPutObject(presignRequest).url().toString();
        return new ImageUploadUrlResponse(uploadUrl, imageKey, EXPIRES_IN_SECONDS);
    }
}
