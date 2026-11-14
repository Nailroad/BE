package nailroad.domain.magazine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "magazine")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Magazine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "magazine_id")
	private Long id;

	@Column(name = "title")
	private String title;

	@Column(name = "content", columnDefinition = "TEXT")
	private String content;

	@Column(name = "thumbnail_image_url")
	private String thumbnailImageUrl;

	@Builder
	public Magazine(String title, String content, String thumbnailImageUrl) {
		this.title = title;
		this.content = content;
		this.thumbnailImageUrl = thumbnailImageUrl;
	}
}
