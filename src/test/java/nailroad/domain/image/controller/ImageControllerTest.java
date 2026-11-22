package nailroad.domain.image.controller;

import java.security.Principal;
import java.util.List;
import nailroad.domain.image.code.ImageErrorCode;
import nailroad.domain.image.dto.ImageUploadUrlRequest;
import nailroad.domain.image.dto.ImageUploadUrlRequest.FileMetadata;
import nailroad.domain.image.dto.ImageUploadUrlResponse;
import nailroad.domain.image.dto.ImageUploadUrlResponse.UploadItem;
import nailroad.domain.image.enums.ImagePurpose;
import nailroad.domain.image.service.ImageUploadUrlService;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import nailroad.global.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ImageControllerTest {

    private static final Principal USER = () -> "test-user";
    private static final List<FileMetadata> FILES = List.of(
            new FileMetadata("image/jpeg", 2048L),
            new FileMetadata("image/heic", 4096L)
    );
    private static final ImageUploadUrlRequest REQUEST = new ImageUploadUrlRequest("DESIGN", null, FILES);

    private final ImageUploadUrlService service = mock(ImageUploadUrlService.class);
    private final ImageController controller = new ImageController(service);

    // 업로드 용도와 파일 목록을 서비스에 전달하고 발급 결과를 반환하는지 확인합니다.
    @Test
    void passesPurposeAndAllFilesToService() {
        ImageUploadUrlResponse issued = new ImageUploadUrlResponse(List.of(
                new UploadItem("https://test.example/photo.jpg", "designs/photo.jpg", 300),
                new UploadItem("https://test.example/photo.heic", "designs/photo.heic", 300)
        ));
        when(service.issue(ImagePurpose.DESIGN, FILES)).thenReturn(issued);

        ApiResponse<ImageUploadUrlResponse> response = controller.issueUploadUrl(USER, REQUEST);

        assertEquals("COMMON200", response.getCode());
        assertSame(issued, response.getResult());
        verify(service).issue(ImagePurpose.DESIGN, FILES);
    }

    // 인증된 사용자가 없으면 URL 발급을 거절하는지 확인합니다.
    @Test
    void requiresAnAuthenticatedPrincipal() {
        CustomException exception = assertThrows(CustomException.class,
                () -> controller.issueUploadUrl(null, REQUEST));

        assertEquals(GlobalErrorCode.UNAUTHORIZED, exception.getErrorCode());
        verifyNoInteractions(service);
    }

    // 지원하지 않는 용도를 보내면 이미지 용도 오류가 발생하는지 확인합니다.
    @ParameterizedTest
    @ValueSource(strings = {"PROFILE", "0"})
    void rejectsUnsupportedPurpose(String purpose) {
        ImageUploadUrlRequest request = new ImageUploadUrlRequest(purpose, null, FILES);

        CustomException exception = assertThrows(CustomException.class,
                () -> controller.issueUploadUrl(USER, request));

        assertEquals(ImageErrorCode.INVALID_PURPOSE, exception.getErrorCode());
        verifyNoInteractions(service);
    }
}
