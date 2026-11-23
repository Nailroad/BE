package nailroad.domain.user.service;

import lombok.RequiredArgsConstructor;
import nailroad.domain.user.dto.LoginResponse;
import nailroad.domain.user.dto.TokenResponse;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.service.RefreshTokenService.RotatedToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final RefreshTokenService refreshTokenService;
	private final JwtService jwtService;

	@Transactional(readOnly = true)
	public LoginResponse loginComplete(String refreshToken) {
		return LoginResponse.from(refreshTokenService.currentUser(refreshToken));
	}

	@Transactional
	public ReissueResult reissue(String refreshToken) {
		RotatedToken rotated = refreshTokenService.rotate(refreshToken);
		User user = rotated.user();
		TokenResponse response = new TokenResponse(jwtService.issue(user.getId()), "Bearer",
			JwtService.ACCESS_TOKEN_TTL.toSeconds(), LoginResponse.from(user));
		return new ReissueResult(response, rotated.value());
	}

	public void logout(String refreshToken) {
		refreshTokenService.revoke(refreshToken);
	}

	public record ReissueResult(TokenResponse response, String refreshToken) {
	}
}
