package nailroad.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "app.auth.phone", name = "mode", havingValue = "log")
public class LoggingPhoneCodeSender implements PhoneCodeSender {
	@Override
	public void send(String phone, String code) {
		log.info("로컬 휴대전화 인증번호: phone={}, code={}", phone, code);
	}
}
