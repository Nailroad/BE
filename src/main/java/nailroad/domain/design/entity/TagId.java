package nailroad.domain.design.entity;

import java.io.Serializable;

/**
 * {@code tag_id}와 {@code design_id}로 구성된 {@link Tag}의 복합 키.
 * 엔티티의 연관관계 매핑과 키 구조를 각각 읽기 쉽도록 별도 타입으로 둔다.
 *
 * @param tagId 태그 ID
 * @param design {@code Tag.design} 연관 필드에 대응하는 디자인의 ID
 */
public record TagId(Long tagId, Long design) implements Serializable {
}
