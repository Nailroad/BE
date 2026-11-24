package nailroad.domain.image.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import java.net.URI;
import java.util.List;
import nailroad.domain.image.code.ImageErrorCode;
import nailroad.domain.image.dto.ImageUploadUrlRequest.FileMetadata;
import nailroad.domain.image.dto.ImageUploadUrlResponse.UploadItem;
import nailroad.domain.image.enums.ImagePurpose;
import nailroad.domain.shop.enums.ShopApprovalStatus;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ImageUploadUrlServiceTest {

    private static final String BUCKET = "test-image-uploads";
    private static final long USER_ID = 7L;
    private static final long SHOP_ID = 17L;

    private final EntityManager entityManager = mock(EntityManager.class);
    private final Tuple shop = mock(Tuple.class);
    private final S3Presigner presigner = spy(S3Presigner.builder()
            .region(Region.AP_NORTHEAST_2)
            .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("test-access-key", "test-secret-key")))
            .build());
    private final ImageUploadUrlService service = new ImageUploadUrlService(presigner, BUCKET, entityManager);

    @BeforeEach
    void setUpApprovedShop() {
        TypedQuery<Tuple> query = mock();
        when(entityManager.createQuery(anyString(), eq(Tuple.class))).thenReturn(query);
        when(query.setParameter("shopId", SHOP_ID)).thenReturn(query);
        when(query.getSingleResultOrNull()).thenReturn(shop);
        when(shop.get("ownerId", Long.class)).thenReturn(USER_ID);
        when(shop.get("approvalStatus", ShopApprovalStatus.class)).thenReturn(ShopApprovalStatus.APPROVED);
    }

    @AfterEach
    void closePresigner() {
        presigner.close();
    }

    // 다른 사용자가 소유한 샵의 업로드 URL을 발급하려 하면 거절하는지 확인합니다.
    @Test
    void rejectsUsersWhoDoNotOwnTheShop() {
        List<FileMetadata> files = List.of(new FileMetadata("image/jpeg", 1024L));

        CustomException exception = assertThrows(CustomException.class,
                () -> service.issueForShop(USER_ID + 1, SHOP_ID, files));

        assertEquals(GlobalErrorCode.FORBIDDEN, exception.getErrorCode());
        verifyNoInteractions(presigner);
    }

    // 승인 대기 중인 샵에는 업로드 URL을 발급하지 않는지 확인합니다.
    @Test
    void rejectsUnapprovedShops() {
        when(shop.get("approvalStatus", ShopApprovalStatus.class)).thenReturn(ShopApprovalStatus.PENDING);
        List<FileMetadata> files = List.of(new FileMetadata("image/jpeg", 1024L));

        CustomException exception = assertThrows(CustomException.class,
                () -> service.issueForShop(USER_ID, SHOP_ID, files));

        assertEquals(GlobalErrorCode.CONFLICT, exception.getErrorCode());
        verifyNoInteractions(presigner);
    }

    // 지원하는 형식별로 객체 키와 만료 시간이 맞고 파일 타입과 크기가 서명에 포함되는지 확인합니다.
    @ParameterizedTest
    @CsvSource({
            "SHOP, image/png, shops/17, png, 1024",
            "DESIGN, image/jpeg, designs, jpg, 3024",
            "REVIEW, image/webp, reviews, webp, 1024",
            "SHOP, image/heic, shops/17, heic, 1234",
            "DESIGNER, image/heif, designers, heif, 5242880"
    })
    void signsUrlWithExpectedKeyContentTypeAndSize(
            ImagePurpose purpose, String contentType, String prefix, String extension, long sizeBytes
    ) {
        List<FileMetadata> files = List.of(new FileMetadata(contentType, sizeBytes));
        UploadItem response = (purpose == ImagePurpose.SHOP
                ? service.issueForShop(USER_ID, SHOP_ID, files)
                : service.issue(purpose, files)).items().getFirst();
        URI uploadUrl = URI.create(response.uploadUrl());

        assertThat(response.imageKey(), matchesPattern(prefix + "/[0-9a-f-]{36}\\." + extension));
        assertEquals(300, response.expiresInSeconds());
        assertEquals("https", uploadUrl.getScheme());
        assertEquals(BUCKET + ".s3.ap-northeast-2.amazonaws.com", uploadUrl.getHost());
        assertEquals("/" + response.imageKey(), uploadUrl.getPath());
        assertThat(uploadUrl.getRawQuery(), containsString("X-Amz-Expires=300"));
        assertThat(uploadUrl.getRawQuery(), containsString("X-Amz-SignedHeaders=content-length%3Bcontent-type%3Bhost"));
    }

    // 요청 순서대로 서로 다른 키를 발급하고 파일별 타입과 크기를 서명하는지 확인합니다.
    @Test
    void issuesDistinctKeysAndSignsEachFilesMetadataInRequestedOrder() {
        List<FileMetadata> files = List.of(
                new FileMetadata("image/png", 5242880L),
                new FileMetadata("image/jpeg", 2048L),
                new FileMetadata("image/png", 1000000L)
        );

        List<String> keys = service.issueForShop(USER_ID, SHOP_ID, files).items().stream()
                .map(UploadItem::imageKey).toList();

        assertEquals(3, keys.size());
        assertThat(keys.get(0), matchesPattern("shops/17/[0-9a-f-]{36}\\.png"));
        assertThat(keys.get(1), matchesPattern("shops/17/[0-9a-f-]{36}\\.jpg"));
        assertThat(keys.get(2), matchesPattern("shops/17/[0-9a-f-]{36}\\.png"));
        assertEquals(3L, keys.stream().distinct().count());

        ArgumentCaptor<PutObjectPresignRequest> signedRequests = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(presigner, times(files.size())).presignPutObject(signedRequests.capture());
        List<PutObjectRequest> objects = signedRequests.getAllValues().stream()
                .map(PutObjectPresignRequest::putObjectRequest)
                .toList();
        assertEquals(keys, objects.stream().map(PutObjectRequest::key).toList());
        assertEquals(files.stream().map(FileMetadata::contentType).toList(),
                objects.stream().map(PutObjectRequest::contentType).toList());
        assertEquals(files.stream().map(FileMetadata::sizeBytes).toList(),
                objects.stream().map(PutObjectRequest::contentLength).toList());
    }

    // MIME 타입이 누락되거나 지원되지 않으면 URL 발급을 거절하는지 확인합니다.
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "text/plain")
    void rejectsUnsupportedContentType(String contentType) {
        CustomException exception = assertThrows(CustomException.class,
                () -> service.issueForShop(USER_ID, SHOP_ID, List.of(new FileMetadata(contentType, 1024L))));

        assertEquals(ImageErrorCode.UNSUPPORTED_CONTENT_TYPE, exception.getErrorCode());
    }

    // 파일 크기가 0 이하이면 URL 발급을 거절하는지 확인합니다.
    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void rejectsNonPositiveFileSize(long sizeBytes) {
        CustomException exception = assertThrows(CustomException.class,
                () -> service.issueForShop(USER_ID, SHOP_ID, List.of(new FileMetadata("image/jpeg", sizeBytes))));

        assertEquals(ImageErrorCode.INVALID_FILE_SIZE, exception.getErrorCode());
    }

    // 5MB를 초과한 파일이 포함되면 서명 전에 요청 전체를 거절하는지 확인합니다.
    @Test
    void rejectsTheWholeRequestBeforeSigningIfAnyFileIsLargerThanFiveMegabytes() {
        S3Presigner unusedPresigner = mock(S3Presigner.class);
        ImageUploadUrlService uploadService = new ImageUploadUrlService(unusedPresigner, BUCKET, entityManager);
        List<FileMetadata> files = List.of(
                new FileMetadata("image/jpeg", 1024L),
                new FileMetadata("image/png", 5242881L)
        );

        CustomException exception = assertThrows(CustomException.class,
                () -> uploadService.issueForShop(USER_ID, SHOP_ID, files));

        assertEquals(ImageErrorCode.FILE_TOO_LARGE, exception.getErrorCode());
        verifyNoInteractions(unusedPresigner);
    }
}
