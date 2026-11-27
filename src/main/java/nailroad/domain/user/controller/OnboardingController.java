package nailroad.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nailroad.domain.auth.dto.LoginResponse;
import nailroad.domain.terms.dto.TermsResponse;
import nailroad.domain.user.dto.CompleteOnboardingRequest;
import nailroad.domain.user.dto.SendPhoneCodeRequest;
import nailroad.domain.user.dto.VerifyPhoneCodeRequest;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.service.OnboardingService;
import nailroad.domain.user.service.PhoneVerificationService;
import nailroad.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

	private final PhoneVerificationService phoneVerificationService;
	private final OnboardingService onboardingService;

	@GetMapping("/terms")
	@Operation(summary = "가입 약관 조회")
	public ApiResponse<List<TermsResponse>> terms() {
		return ApiResponse.onSuccess("가입 약관을 조회했습니다.", onboardingService.terms());
	}

	@PostMapping("/phone/send")
	@Operation(summary = "휴대전화 인증번호 발송")
	public ApiResponse<Void> sendPhoneCode(@Parameter(hidden = true) @AuthenticationPrincipal User user,
			@Valid @RequestBody SendPhoneCodeRequest request) {
		phoneVerificationService.send(user.getId(), request.phone());
		return ApiResponse.onSuccess("인증번호를 발송했습니다.");
	}

	@PostMapping("/phone/verify")
	@Operation(summary = "휴대전화 인증번호 확인")
	public ApiResponse<Void> verifyPhoneCode(@Parameter(hidden = true) @AuthenticationPrincipal User user,
			@Valid @RequestBody VerifyPhoneCodeRequest request) {
		phoneVerificationService.verify(user.getId(), request.phone(), request.code());
		return ApiResponse.onSuccess("휴대전화 인증이 완료되었습니다.");
	}

	@PostMapping
	@Operation(summary = "회원가입 추가정보 입력")
	public ApiResponse<LoginResponse> complete(@Parameter(hidden = true) @AuthenticationPrincipal User user,
			@Valid @RequestBody CompleteOnboardingRequest request) {
		return ApiResponse.onSuccess("회원가입이 완료되었습니다.", onboardingService.complete(user.getId(), request));
	}
}
