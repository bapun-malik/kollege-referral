package com.kollege.referral.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@Schema(description = "Response object containing referral analytics data")
public class ReferralAnalyticsResponse {

    @Schema(description = "Overview of referral performance")
    private Overview overview;

    @Schema(description = "Trends data for different time periods")
    private Trends trends;

    @Schema(description = "List of top performing referrers")
    private List<TopPerformer> topPerformers;

    @Data
    @Builder
    public static class Overview {
        @Schema(description = "Total number of referrals", example = "150")
        private Integer totalReferrals;

        @Schema(description = "Number of active referrers", example = "45")
        private Integer activeReferrers;

        @Schema(description = "Conversion rate percentage", example = "35.5")
        private Double conversionRate;

        @Schema(description = "Average earnings per referral", example = "75.50")
        private Double averageEarningsPerReferral;
    }

    @Data
    @Builder
    public static class Trends {
        @Schema(description = "Daily trend data")
        private List<DailyTrend> daily;
    }

    @Data
    @Builder
    public static class DailyTrend {
        @Schema(description = "Date of the trend data", example = "2024-03-15")
        private String date;

        @Schema(description = "Number of referrals on this date", example = "12")
        private Integer referrals;

        @Schema(description = "Number of conversions on this date", example = "5")
        private Integer conversions;

        @Schema(description = "Total earnings on this date", example = "375.00")
        private Double earnings;
    }

    @Data
    @Builder
    public static class TopPerformer {
        @Schema(description = "User ID of the referrer", example = "1")
        private Long userId;

        @Schema(description = "Name of the referrer", example = "John Doe")
        private String name;

        @Schema(description = "Total number of referrals", example = "25")
        private Integer totalReferrals;

        @Schema(description = "Total earnings from referrals", example = "1875.00")
        private Double totalEarnings;

        @Schema(description = "Conversion rate percentage", example = "40.0")
        private Double conversionRate;
    }
}