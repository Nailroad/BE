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
@Table(name = "designer")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Designer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "designer_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "specialties")
	private String specialties;

	@Builder
	public Designer(Shop shop, String name, String profileImageUrl, String description, String specialties) {
		this.shop = shop;
		this.name = name;
		this.profileImageUrl = profileImageUrl;
		this.description = description;
		this.specialties = specialties;
	}
}
