package nailroad.domain.notification.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import nailroad.domain.notification.dto.response.NotificationQueryResponse.ListResponse;
import nailroad.domain.notification.repository.NotificationRepository;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 100;

	private final NotificationRepository notificationRepository;

	public ListResponse getList(Long userId, Long cursor, Integer size) {
		int pageSize = Objects.requireNonNullElse(size, DEFAULT_PAGE_SIZE);
		if (pageSize < 1 || pageSize > MAX_PAGE_SIZE || (cursor != null && cursor < 1)) {
			throw new CustomException(GlobalErrorCode.BAD_REQUEST);
		}
		long maxExclusiveId = Objects.requireNonNullElse(cursor, Long.MAX_VALUE);
		return ListResponse.from(notificationRepository.findPage(userId, maxExclusiveId, PageRequest.ofSize(pageSize)));
	}
}
