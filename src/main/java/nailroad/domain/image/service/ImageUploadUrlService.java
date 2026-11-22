package nailroad.domain.image.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import nailroad.domain.image.code.ImageErrorCode;
import nailroad.domain.image.dto.ImageUploadUrlRequest.FileMetadata;
import nailroad.domain.image.dto.ImageUploadUrlResponse;
import nailroad.domain.image.dto.ImageUploadUrlResponse.UploadItem;
import nailroad.domain.image.enums.ImagePurpose;
import nailroad.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class ImageUploadUrlService {

    private static final int EXPIRES_IN_SECONDS = 300;
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/heic", "heic",
            "image/heif", "heif"
    );

    private final S3Presigner presigner;
    private final String bucket;

    public ImageUploadUrlService(S3Presigner presigner, @Value("${app.s3.bucket}") String bucket) {
        this.presigner = presigner;
        this.bucket = bucket;
    }

    public ImageUploadUrlResponse issue(ImagePurpose purpose, List<FileMetadata> files) {
        List<PutObjectRequest> objectRequests = files.stream()
                .map(file -> prepareUpload(purpose, file))
                .toList();

        List<UploadItem> items = objectRequests.stream().map(this::signUpload).toList();
        return new ImageUploadUrlResponse(items);
    }

    private PutObjectRequest prepareUpload(ImagePurpose purpose, FileMetadata file) {
        long sizeBytes = file.sizeBytes();
        if (sizeBytes <= 0) {
            throw new CustomException(ImageErrorCode.INVALID_FILE_SIZE);
        }
        if (sizeBytes > MAX_FILE_SIZE_BYTES) {
            throw new CustomException(ImageErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.contentType();
        String extension = contentType == null ? null : IMAGE_EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new CustomException(ImageErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }

        String imageKey = purpose.getKeyPrefix() + "/" + UUID.randomUUID() + "." + extension;
        return PutObjectRequest.builder()
                .bucket(bucket)
                .key(imageKey)
                .contentType(contentType)
                // 브라우저가 보내는 실제 Content-Length를 서명의 파일 크기와 일치시킵니다.
                .contentLength(sizeBytes)
                .build();
    }

    private UploadItem signUpload(PutObjectRequest objectRequest) {
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(EXPIRES_IN_SECONDS))
                .putObjectRequest(objectRequest)
                .build();

        String uploadUrl = presigner.presignPutObject(presignRequest).url().toString();
        return new UploadItem(uploadUrl, objectRequest.key(), EXPIRES_IN_SECONDS);
    }
}
