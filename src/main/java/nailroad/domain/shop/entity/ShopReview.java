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
import nailroad.domain.user.entity.User;

@Getter
@Entity
@Table(name = "shop_review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopReview {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "shopreview_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "content")
	private String content;

	@Column(name = "rating", nullable = false)
	private Float rating;

	@Column(name = "review_image_url")
	private String reviewImageUrl;

	@Builder
	public ShopReview(Shop shop, User user, String content, Float rating, String reviewImageUrl) {
		this.shop = shop;
		this.user = user;
		this.content = content;
		this.rating = rating;
		this.reviewImageUrl = reviewImageUrl;
	}
}
