package nailroad.domain.auth.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds, LoginResponse user) {
}
