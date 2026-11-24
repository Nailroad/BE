package nailroad.domain.image.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class CloudFrontImageUrlService {
    private final String baseUrl;

    public CloudFrontImageUrlService(@Value("${app.cloudfront.base-url}") String baseUrl) {
        URI uri = URI.create(baseUrl.strip());
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                || !(uri.getPath().isEmpty() || uri.getPath().equals("/"))) {
            throw new IllegalArgumentException("CLOUDFRONT_BASE_URL must be an HTTPS origin without a path");
        }
        this.baseUrl = uri.getScheme() + "://" + uri.getRawAuthority();
    }

    public String readUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String encodedKey = Arrays.stream(key.split("/", -1))
                .map(segment -> UriUtils.encode(segment, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));
        return baseUrl + "/" + encodedKey;
    }
}
