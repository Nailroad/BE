package nailroad.domain.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import nailroad.domain.user.enums.AccountType;

public record CompleteOnboardingRequest(@NotNull AccountType accountType, @NotBlank String phone,
		@NotEmpty List<@Valid TermAgreement> agreements) {

	public record TermAgreement(@NotNull Long termId, @NotNull Boolean agreed) {
	}
}
