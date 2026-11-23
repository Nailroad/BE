package nailroad.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nailroad.domain.user.code.AuthErrorCode;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.enums.AccountType;
import nailroad.domain.user.repository.UserRepository;
import nailroad.domain.user.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

/** SecurityConfig에서 직접 생성해 Security 필터 체인에만 넣는다. (빈으로 등록하지 않음) */
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String token = accessToken(request.getHeader(HttpHeaders.AUTHORIZATION));
		if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				Long userId = Long.valueOf(jwtService.decode(token).getSubject());
				boolean found = userRepository.findById(userId)
					.filter(user -> user.getDeletedAt() == null)
					.map(user -> {
						authenticate(user);
						return true;
					})
					.orElse(false);
				if (!found) {
					request.setAttribute("authErrorCode", AuthErrorCode.USER_NOT_FOUND);
				}
			} catch (JwtException | NumberFormatException ignored) {
				SecurityContextHolder.clearContext();
				request.setAttribute("authErrorCode", AuthErrorCode.INVALID_ACCESS_TOKEN);
			}
		}
		chain.doFilter(request, response);
	}

	private String accessToken(String authorization) {
		if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return null;
		}
		return authorization.substring(7).trim();
	}

	private void authenticate(User user) {
		List<SimpleGrantedAuthority> authorities;
		if (user.getOnboardingCompletedAt() == null || user.getAccountType() == null) {
			authorities = List.of(new SimpleGrantedAuthority("ROLE_ONBOARDING"));
		} else {
			String accountRole = user.getAccountType() == AccountType.SHOP_OWNER
				? "ROLE_SHOP_OWNER" : "ROLE_CUSTOMER";
			authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"),
				new SimpleGrantedAuthority(accountRole));
		}
		var authentication = new UsernamePasswordAuthenticationToken(
			user, null, authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
