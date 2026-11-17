package nailroad.domain.shop.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nailroad.domain.shop.enums.ShopApprovalStatus;
import nailroad.domain.shop.enums.ShopFacility;
import nailroad.domain.shop.enums.ShopStatus;
import nailroad.domain.user.entity.User;
import nailroad.global.common.BaseEntity;

@Getter
@Entity
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

	@Column(name = "shop_address")
	private String shopAddress;

	@Column(name = "longitude", precision = 10, scale = 7)
	private BigDecimal longitude;

	@Column(name = "latitude", precision = 10, scale = 7)
	private BigDecimal latitude;

	@Column(name = "business_hour")
	private String businessHour;

	@Enumerated(EnumType.STRING)
	@Column(name = "approval_status")
	private ShopApprovalStatus approvalStatus;

	@Column(name = "shop_image")
	private String shopImage;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private ShopStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "facility")
	private ShopFacility facility;

	@Column(name = "location")
	private String location;

	@Builder
	public Shop(
		User user,
		String shopName,
		String shopAddress,
		BigDecimal longitude,
		BigDecimal latitude,
		String businessHour,
		ShopApprovalStatus approvalStatus,
		String shopImage,
		ShopStatus status,
		ShopFacility facility,
		String location
	) {
		this.user = user;
		this.shopName = shopName;
		this.shopAddress = shopAddress;
		this.longitude = longitude;
		this.latitude = latitude;
		this.businessHour = businessHour;
		this.approvalStatus = approvalStatus;
		this.shopImage = shopImage;
		this.status = status;
		this.facility = facility;
		this.location = location;
	}
}
