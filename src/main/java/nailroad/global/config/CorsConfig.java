package nailroad.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3030",
                        "https://nailroad.site",
                        "https://www.nailroad.site")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE")
                .allowedHeaders("Content-Type", "Authorization", "X-XSRF-TOKEN")
                //reissue에 쿠키가 실리게 하는 역할
                .allowCredentials(true);
    }
}
