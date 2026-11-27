package nailroad.domain.shop.dto.response;

public record DesignerResponse(
	Long designerId,
	String name,
	String profileImageUrl,
	String description,
	String specialties
) {
}
