package nailroad.domain.image.controller;

import nailroad.domain.image.service.ImageUploadUrlService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ImageController.class, properties = "app.s3.bucket=test-image-uploads")
@Import({ImageUploadUrlService.class, ImageControllerTest.S3TestConfiguration.class})
class ImageControllerTest {

    private static final String PATH = "/api/v1/images/presigned-url";

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @CsvSource({
            "SHOP, image/png, shops, png",
            "DESIGN, image/jpeg, designs, jpg",
            "REVIEW, image/webp, reviews, webp"
    })
    void issuesUploadUrlForExpectedImageKey(String purpose, String contentType, String prefix, String extension)
            throws Exception {
        mockMvc.perform(post(PATH)
                        .principal(() -> "test-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"purpose":"%s","contentType":"%s"}
                                """.formatted(purpose, contentType)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.imageKey", matchesPattern(prefix + "/[0-9a-f-]{36}\\." + extension)))
                .andExpect(jsonPath("$.result.expiresInSeconds").value(300))
                .andExpect(jsonPath("$.result.uploadUrl", allOf(
                        startsWith("https://test-image-uploads.s3.ap-northeast-2.amazonaws.com/" + prefix + "/"),
                        containsString("X-Amz-Expires=300"),
                        containsString("X-Amz-SignedHeaders=content-type%3Bhost"),
                        containsString("X-Amz-Signature=")
                )));
    }

    @Test
    void requiresAnAuthenticatedPrincipal() throws Exception {
        mockMvc.perform(post(PATH)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer unverified-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"purpose":"SHOP","contentType":"image/jpeg"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMMON401"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"PROFILE\"", "0", "\"0\""})
    void rejectsUnsupportedPurpose(String purpose) throws Exception {
        mockMvc.perform(post(PATH)
                        .principal(() -> "test-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"purpose":%s,"contentType":"image/jpeg"}
                                """.formatted(purpose)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"""
            {"purpose":"SHOP","contentType":"text/plain"}
            """, """
            {"purpose":"SHOP"}
            """, """
            {"contentType":"image/jpeg"}
            """})
    void rejectsUnsupportedMediaOrMissingInput(String body) throws Exception {
        mockMvc.perform(post(PATH)
                        .principal(() -> "test-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION400"));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class S3TestConfiguration {

        @Bean(destroyMethod = "close")
        S3Presigner s3Presigner() {
            return S3Presigner.builder()
                    .region(Region.AP_NORTHEAST_2)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("test-access-key", "test-secret-key")))
                    .build();
        }
    }
}
