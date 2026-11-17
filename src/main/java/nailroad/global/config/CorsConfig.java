package nailroad.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3030",
                        "https://nailroad.site",
                        "https://www.nailroad.site")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE")
                .allowedHeaders("*");
    }
}
