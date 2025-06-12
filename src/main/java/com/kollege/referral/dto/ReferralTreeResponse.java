package com.kollege.referral.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@Schema(description = "Response object containing user's referral tree")
public class ReferralTreeResponse {

    @Schema(description = "ID of the user whose referral tree is being returned", example = "1")
    private Long userId;

    @Schema(description = "Direct (level 1) referrals")
    private ReferralGroup directReferrals;

    @Schema(description = "Indirect (level 2) referrals")
    private ReferralGroup indirectReferrals;

    @Data
    @Builder
    public static class ReferralGroup {
        @Schema(description = "Number of referrals in this group", example = "2")
        private int count;

        @Schema(description = "List of referred users")
        private List<ReferredUser> users;
    }

    @Data
    @Builder
    public static class ReferredUser {
        @Schema(description = "User ID", example = "2")
        private Long id;

        @Schema(description = "User's name", example = "Jane Smith")
        private String name;

        @Schema(description = "User's registration date", example = "2024-03-15T10:30:00Z")
        private String registrationDate;

        @Schema(description = "Total purchase amount by the user", example = "500.00")
        private Double totalPurchases;
    }
}