package nailroad.domain.magazine.entity;

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
import nailroad.domain.design.entity.Design;

@Getter
@Entity
@Table(name = "magazine_design")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MagazineDesign {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "magazine_design_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "magazine_id", nullable = false)
	private Magazine magazine;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "design_id", nullable = false)
	private Design design;

	@Builder
	public MagazineDesign(Magazine magazine, Design design) {
		this.magazine = magazine;
		this.design = design;
	}
}
