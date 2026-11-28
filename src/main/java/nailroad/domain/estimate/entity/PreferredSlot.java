package nailroad.domain.estimate.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import nailroad.domain.estimate.enums.SlotTimeType;

/**
 * 견적 요청의 희망 일정 하나. estimate.preferred_slots(JSON)에 목록으로 저장한다.
 * 시간 무관(ANY)이면 시간을 비우고, 시간 지정(RANGE)이면 시작·종료 시간을 모두 넣는다.
 */
public record PreferredSlot(
	@NotNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate date,
	@NotNull SlotTimeType timeType,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime startTime,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime endTime
) {

	public static PreferredSlot anyTime(LocalDate date) {
		return new PreferredSlot(date, SlotTimeType.ANY, null, null);
	}

	public static PreferredSlot range(LocalDate date, LocalTime startTime, LocalTime endTime) {
		return new PreferredSlot(date, SlotTimeType.RANGE, startTime, endTime);
	}

	@JsonIgnore
	@AssertTrue(message = "시간 무관이면 시간을 비우고, 시간 지정이면 시작 시간이 종료 시간보다 빨라야 합니다.")
	public boolean isTimeRangeValid() {
		if (timeType == null) {
			return true;
		}
		if (timeType == SlotTimeType.ANY) {
			return startTime == null && endTime == null;
		}
		return startTime != null && endTime != null && startTime.isBefore(endTime);
	}
}
