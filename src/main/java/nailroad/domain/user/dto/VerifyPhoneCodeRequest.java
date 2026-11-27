package nailroad.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyPhoneCodeRequest(@NotBlank String phone,
		@Pattern(regexp = "\\d{6}") String code) {
}
