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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nailroad.domain.user.entity.User;
import nailroad.global.common.BaseCreatedEntity;

@Getter
@Entity
@Table(
	name = "shop_like",
	uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "shop_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopLike extends BaseCreatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "shop_like_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	@Column(name = "is_liked")
	private Boolean liked;

	@Builder
	public ShopLike(User user, Shop shop, Boolean liked) {
		this.user = user;
		this.shop = shop;
		this.liked = liked;
	}
}
