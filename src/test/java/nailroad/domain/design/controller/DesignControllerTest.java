package nailroad.domain.design.controller;

import nailroad.domain.design.code.DesignErrorCode;
import nailroad.domain.design.dto.DesignDetailResponse;
import nailroad.domain.design.service.DesignService;
import nailroad.global.exception.CustomException;
import nailroad.global.response.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DesignControllerTest {
    private final DesignService service = mock(DesignService.class);
    private final DesignController controller = new DesignController(service);

    @Test
    void passesNumericDesignIdToService() {
        DesignDetailResponse detail = mock(DesignDetailResponse.class);
        when(service.getDetail(3L, 87L)).thenReturn(detail);

        ApiResponse<DesignDetailResponse> response = controller.getDesign(3L, "87");

        assertEquals("COMMON200", response.getCode());
        assertEquals("디자인 상세 정보를 조회했습니다.", response.getMessage());
        assertSame(detail, response.getResult());
        verify(service).getDetail(3L, 87L);
    }

    @Test
    void rejectsNonNumericAndOverflowingDesignIdWithDesignCode() {
        CustomException nonNumeric = assertThrows(CustomException.class,
                () -> controller.getDesign(3L, "abc"));
        CustomException overflowing = assertThrows(CustomException.class,
                () -> controller.getDesign(3L, "999999999999999999999"));

        assertEquals(DesignErrorCode.INVALID_DESIGN_ID, nonNumeric.getErrorCode());
        assertEquals(DesignErrorCode.INVALID_DESIGN_ID, overflowing.getErrorCode());
        verifyNoInteractions(service);
    }
}
