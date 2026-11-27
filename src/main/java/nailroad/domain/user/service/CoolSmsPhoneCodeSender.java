package nailroad.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import nailroad.domain.user.code.PhoneErrorCode;
import nailroad.domain.user.exception.PhoneException;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** CoolSMS로 인증번호를 보낸다. 발신번호는 CoolSMS 콘솔에 미리 등록된 번호여야 한다. */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "app.auth.phone", name = "mode", havingValue = "coolsms")
public class CoolSmsPhoneCodeSender implements PhoneCodeSender {

	private static final String API_URL = "https://api.coolsms.co.kr";

	private final DefaultMessageService messageService;
	private final String from;

	public CoolSmsPhoneCodeSender(
			@Value("${app.auth.phone.coolsms.api-key}") String apiKey,
			@Value("${app.auth.phone.coolsms.api-secret}") String apiSecret,
			@Value("${app.auth.phone.coolsms.sender}") String sender) {
		if (apiKey.isBlank() || apiSecret.isBlank() || sender.isBlank()) {
			throw new IllegalStateException(
				"PHONE_VERIFICATION_MODE=coolsms requires COOLSMS_API_KEY, COOLSMS_API_SECRET and COOLSMS_SENDER");
		}
		this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, API_URL);
		this.from = sender.replace("-", "");
	}

	@Override
	public void send(String phone, String code) {
		Message message = new Message();
		message.setFrom(from);
		message.setTo(phone);
		// 한글 45자 이내로 유지해야 단문(SMS) 요금으로 발송된다.
		message.setText("[네일로드] 인증번호는 " + code + " 입니다.");
		try {
			messageService.sendOne(new SingleMessageSendingRequest(message));
		} catch (Exception exception) {
			// 인증번호와 수신번호는 남기지 않는다.
			log.error("CoolSMS 인증번호 발송 실패: {} {}", exception.getClass().getSimpleName(), exception.getMessage());
			throw new PhoneException(PhoneErrorCode.DELIVERY_UNAVAILABLE);
		}
	}
}
