package nailroad.domain.design.entity;

import jakarta.persistence.*;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nailroad.domain.design.enums.DesignCategoryCode;
import nailroad.domain.design.enums.DesignColorCode;
import nailroad.domain.design.enums.DesignShapeCode;
import nailroad.domain.shop.entity.Shop;

@Getter
@Entity
@Table(name = "design")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Design {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "design_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	@Column(name = "name")
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "category_code")
	private List<DesignCategoryCode> categoryCodes;

	@Enumerated(EnumType.STRING)
	@Column(name = "shape_code")
	private List<DesignShapeCode> shapeCodes;

	@Enumerated(EnumType.STRING)
	@Column(name = "color_code")
	private List<DesignColorCode> colorCodes;

	@Column(name = "design_image_url")
	private String designImageUrl;

	@Builder
	public Design(
		Shop shop,
		String name,
		List<DesignCategoryCode> categoryCodes,
		List<DesignShapeCode> shapeCodes,
		List<DesignColorCode> colorCodes,
		String designImageUrl
	) {
		this.shop = shop;
		this.name = name;
		this.categoryCodes = categoryCodes;
		this.shapeCodes = shapeCodes;
		this.colorCodes = colorCodes;
		this.designImageUrl = designImageUrl;
	}
}
