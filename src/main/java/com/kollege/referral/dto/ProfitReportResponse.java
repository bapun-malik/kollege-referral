package com.kollege.referral.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@Schema(description = "Response object containing profit report data")
public class ProfitReportResponse {

    @Schema(description = "Summary of financial performance")
    private Summary summary;

    @Schema(description = "Detailed breakdown of costs and payouts")
    private Breakdown breakdown;

    @Schema(description = "Monthly trend data")
    private List<MonthlyTrend> monthlyTrends;

    @Schema(description = "Future projections")
    private Projections projections;

    @Data
    @Builder
    public static class Summary {
        @Schema(description = "Total revenue generated", example = "25000.00")
        private Double totalRevenue;

        @Schema(description = "Total amount paid out in referral commissions", example = "2500.00")
        private Double totalPayout;

        @Schema(description = "Net profit after all costs", example = "22500.00")
        private Double netProfit;

        @Schema(description = "Profit margin percentage", example = "90.0")
        private Double profitMargin;
    }

    @Data
    @Builder
    public static class Breakdown {
        @Schema(description = "Amount paid for direct referrals", example = "2000.00")
        private Double directReferralPayouts;

        @Schema(description = "Amount paid for indirect referrals", example = "500.00")
        private Double indirectReferralPayouts;

        @Schema(description = "Payment processing fees", example = "250.00")
        private Double processingFees;

        @Schema(description = "Marketing and promotional costs", example = "1000.00")
        private Double marketingCosts;
    }

    @Data
    @Builder
    public static class MonthlyTrend {
        @Schema(description = "Month of the trend data", example = "2024-03")
        private String month;

        @Schema(description = "Revenue for the month", example = "8500.00")
        private Double revenue;

        @Schema(description = "Total payouts for the month", example = "850.00")
        private Double payouts;

        @Schema(description = "Net profit for the month", example = "7650.00")
        private Double profit;
    }

    @Data
    @Builder
    public static class Projections {
        @Schema(description = "Next month projections")
        private Projection nextMonth;

        @Schema(description = "Next quarter projections")
        private Projection nextQuarter;
    }

    @Data
    @Builder
    public static class Projection {
        @Schema(description = "Expected revenue", example = "9000.00")
        private Double expectedRevenue;

        @Schema(description = "Expected payout amount", example = "900.00")
        private Double expectedPayouts;

        @Schema(description = "Expected net profit", example = "8100.00")
        private Double expectedProfit;
    }
}