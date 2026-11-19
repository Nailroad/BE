package nailroad.domain.shop.dto.response;

import nailroad.domain.shop.enums.ShopApprovalStatus;

public record ShopRegistrationResponse(Long shopId, ShopApprovalStatus approvalStatus) {
}
