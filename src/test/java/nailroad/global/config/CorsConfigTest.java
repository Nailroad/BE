package nailroad.global.config;

import jakarta.servlet.Filter;
import nailroad.domain.design.controller.DesignController;
import nailroad.domain.design.service.DesignService;
import nailroad.domain.user.repository.UserRepository;
import nailroad.domain.user.service.JwtService;
import nailroad.global.security.handler.CustomAccessDenied;
import nailroad.global.security.handler.CustomEntryPoint;
import nailroad.global.security.handler.KakaoLoginFailureHandler;
import nailroad.global.security.handler.KakaoLoginSuccessHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 실제 운영과 같이 SecurityConfig의 CORS 처리(= CorsConfig 설정 사용)를 거쳐 검증한다. */
@WebMvcTest(DesignController.class)
@Import({CorsConfig.class, SecurityConfig.class})
class CorsConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private DesignService designService;

    // SecurityConfig 생성에 필요한 의존성
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private KakaoLoginSuccessHandler kakaoLoginSuccessHandler;
    @MockitoBean
    private KakaoLoginFailureHandler kakaoLoginFailureHandler;
    @MockitoBean
    private CustomEntryPoint customEntryPoint;
    @MockitoBean
    private CustomAccessDenied customAccessDenied;
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    // Boot 4의 @WebMvcTest는 Security 필터를 MockMvc에 자동으로 붙이지 않으므로 직접 연결한다.
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(context.getBean("springSecurityFilterChain", Filter.class))
                .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:3030",
            "https://nailroad.site",
            "https://www.nailroad.site"
    })
    void allowsApiAndPreflightRequestsFromConfiguredOrigins(String origin) throws Exception {
        mockMvc.perform(get("/api/v1/designs/1")
                        .header(HttpHeaders.ORIGIN, origin))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin));

        mockMvc.perform(options("/api/v1/designs/1")
                        .header(HttpHeaders.ORIGIN, origin)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        "Authorization, Content-Type"));
    }

    @Test
    void rejectsPreflightRequestsFromUnconfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/designs/1")
                        .header(HttpHeaders.ORIGIN, "https://untrusted.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
