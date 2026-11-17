package nailroad.domain.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shop_price")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopPrice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "shop_price_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	@Column(name = "service_category")
	private String serviceCategory;

	@Column(name = "service_name")
	private String serviceName;

	@Column(name = "service_price")
	private Integer servicePrice;

	@Builder
	public ShopPrice(Shop shop, String serviceCategory, String serviceName, Integer servicePrice) {
		this.shop = shop;
		this.serviceCategory = serviceCategory;
		this.serviceName = serviceName;
		this.servicePrice = servicePrice;
	}
}
