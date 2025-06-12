package com.kollege.referral.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Response object containing earnings summary")
public class EarningsSummaryResponse {

    @Schema(description = "Total earnings across all referral levels", example = "1250.00")
    private Double totalEarnings;

    @Schema(description = "Earnings from direct referrals (level 1)", example = "1000.00")
    private Double directReferralEarnings;

    @Schema(description = "Earnings from indirect referrals (level 2)", example = "250.00")
    private Double indirectReferralEarnings;

    @Schema(description = "Amount pending for payout", example = "100.00")
    private Double pendingPayouts;

    @Schema(description = "Monthly earnings statistics")
    private MonthlyStats monthlyStats;

    @Data
    @Builder
    public static class MonthlyStats {
        @Schema(description = "Earnings in the current month", example = "450.00")
        private Double currentMonth;

        @Schema(description = "Earnings in the previous month", example = "800.00")
        private Double previousMonth;

        @Schema(description = "Percentage growth from previous month", example = "-43.75")
        private Double monthlyGrowth;
    }
}