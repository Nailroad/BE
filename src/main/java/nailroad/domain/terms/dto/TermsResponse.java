package nailroad.domain.terms.dto;

import nailroad.domain.terms.entity.Terms;

public record TermsResponse(Long termId, String version, String content, boolean required) {
	public static TermsResponse from(Terms terms) {
		return new TermsResponse(terms.getId(), terms.getTermsVersion(), terms.getContent(),
			Boolean.TRUE.equals(terms.getRequired()));
	}
}
