package nailroad.domain.user.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds, LoginResponse user) {
}
