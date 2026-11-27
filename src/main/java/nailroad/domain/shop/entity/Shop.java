package nailroad.domain.shop.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nailroad.domain.shop.enums.ShopApprovalStatus;
import nailroad.domain.shop.enums.ShopFacility;
import nailroad.domain.shop.enums.ShopStatus;
import nailroad.domain.user.entity.User;
import nailroad.global.common.BaseEntity;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@DynamicUpdate
@Table(name = "shop")
@AttributeOverride(name = "createdAt", column = @Column(name = "create_at", nullable = false, updatable = false))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shop extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "shop_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "shop_name")
	private String shopName;

	@JsonIgnore
	@Column(name = "shop_owner_phone", length = 20, updatable = false)
	private String shopOwnerPhone;

	@Column(name = "shop_phone", length = 20)
	private String shopPhone;

	@Column(name = "shop_address")
	private String shopAddress;

	@Column(name = "detail_address")
	private String detailAddress;

	@Column(name = "longitude", precision = 10, scale = 7)
	private BigDecimal longitude;

	@Column(name = "latitude", precision = 10, scale = 7)
	private BigDecimal latitude;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "business_hour", columnDefinition = "jsonb")
	private Map<DayOfWeek, DayHours> businessHour;

	@Enumerated(EnumType.STRING)
	@Column(name = "approval_status")
	private ShopApprovalStatus approvalStatus;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "shop_image", length = 1024)
	private List<String> shopImage;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private ShopStatus status;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "facility")
	private List<ShopFacility> facility;

	@Column(name = "location")
	private String location;

	@Builder
	public Shop(
		User user,
		String shopName,
		String shopOwnerPhone,
		String shopPhone,
		String shopAddress,
		String detailAddress,
		BigDecimal longitude,
		BigDecimal latitude,
		Map<DayOfWeek, DayHours> businessHour,
		ShopApprovalStatus approvalStatus,
		List<String> shopImage,
		ShopStatus status,
		List<ShopFacility> facility,
		String location
	) {
		this.user = user;
		this.shopName = shopName;
		this.shopOwnerPhone = shopOwnerPhone;
		this.shopPhone = shopPhone;
		this.shopAddress = shopAddress;
		this.detailAddress = detailAddress;
		this.longitude = longitude;
		this.latitude = latitude;
		this.businessHour = businessHour;
		this.approvalStatus = approvalStatus;
		this.shopImage = shopImage;
		this.status = status;
		this.facility = facility;
		this.location = location;
	}

	public void updateBasicInfo(
		String shopPhone, Map<DayOfWeek, DayHours> businessHours,
		ShopStatus status, List<ShopFacility> facilities, List<String> imageKeys
	) {
		if (shopPhone != null) {
			this.shopPhone = shopPhone;
		}
		if (businessHours != null) {
			this.businessHour = Map.copyOf(businessHours);
		}
		if (status != null) {
			this.status = status;
		}
		if (facilities != null) {
			this.facility = List.copyOf(facilities);
		}
		if (imageKeys != null) {
			this.shopImage = List.copyOf(imageKeys);
		}
	}

	public Map<DayOfWeek, DayHours> getBusinessHour() {
		return businessHour == null ? Map.of() : Map.copyOf(businessHour);
	}

	public List<String> getShopImage() {
		return shopImage == null ? List.of() : List.copyOf(shopImage);
	}

	public List<ShopFacility> getFacility() {
		return facility == null ? List.of() : List.copyOf(facility);
	}

	public record DayHours(
		@NotNull Boolean closed,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime opensAt,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime closesAt
	) {
		@JsonIgnore
		@AssertTrue(message = "영업일에는 시작 / 종료 시간을 입력하고 휴무일에는 시간을 비워 주세요.")
		public boolean isTimeRangeValid() {
			if (closed == null) {
				return true;
			}
			return closed ? opensAt == null && closesAt == null : opensAt != null && closesAt != null;
		}
	}
}
