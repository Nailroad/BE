package nailroad.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record SendPhoneCodeRequest(@NotBlank String phone) {
}
