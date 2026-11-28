package nailroad.domain.notification.service;

import lombok.RequiredArgsConstructor;
import nailroad.domain.auth.code.AuthErrorCode;
import nailroad.domain.notification.dto.response.PushSettingResponse;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.repository.UserRepository;
import nailroad.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushSettingService {

	private final UserRepository userRepository;

	@Transactional
	public PushSettingResponse change(Long userId, boolean pushEnabled) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
		user.changePushEnabled(pushEnabled);
		return PushSettingResponse.from(user);
	}
}
