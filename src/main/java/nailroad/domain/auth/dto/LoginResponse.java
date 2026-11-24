package nailroad.domain.auth.dto;

import nailroad.domain.user.entity.User;
import nailroad.domain.user.enums.AccountType;

public record LoginResponse(Long userId, String name, String email, AccountType accountType,
		boolean onboardingComplete) {
	public static LoginResponse from(User user) {
		return new LoginResponse(user.getId(), user.getName(), user.getEmail(), user.getAccountType(),
			user.isOnboardingComplete());
	}
}
