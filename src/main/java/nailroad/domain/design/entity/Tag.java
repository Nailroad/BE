package nailroad.domain.design.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nailroad.domain.design.enums.TagName;

@Getter
@Entity
@Table(name = "tag")
@IdClass(TagId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

	@Id
	@Column(name = "tag_id", nullable = false)
	private Long tagId;

	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "design_id", nullable = false)
	private Design design;

	@Enumerated(EnumType.STRING)
	@Column(name = "tag_name", nullable = false)
	private TagName tagName;

	@Builder
	public Tag(Long tagId, Design design, TagName tagName) {
		this.tagId = tagId;
		this.design = design;
		this.tagName = tagName;
	}
}
