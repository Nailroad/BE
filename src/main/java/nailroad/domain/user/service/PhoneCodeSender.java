package nailroad.domain.user.service;

public interface PhoneCodeSender {
	void send(String phone, String code);
}
