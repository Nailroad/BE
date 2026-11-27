package nailroad.domain.user.service;

import nailroad.domain.user.code.PhoneErrorCode;
import nailroad.domain.user.exception.PhoneException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.auth.phone", name = "mode", havingValue = "disabled", matchIfMissing = true)
public class DisabledPhoneCodeSender implements PhoneCodeSender {
	@Override
	public void send(String phone, String code) {
		throw new PhoneException(PhoneErrorCode.DELIVERY_UNAVAILABLE);
	}
}
