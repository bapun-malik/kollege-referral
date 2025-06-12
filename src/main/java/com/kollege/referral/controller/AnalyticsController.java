package com.kollege.referral.controller;

import com.kollege.referral.dto.ReferralAnalyticsResponse;
import com.kollege.referral.dto.ProfitReportResponse;
import com.kollege.referral.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "APIs for retrieving referral system analytics and reports")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get referral analytics", description = "Retrieve comprehensive analytics about referral performance including conversion rates, trends, and top performers", responses = {
            @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReferralAnalyticsResponse.class), examples = @ExampleObject(value = """
                    {
                        "overview": {
                            "totalReferrals": 150,
                            "activeReferrers": 45,
                            "conversionRate": 35.5,
                            "averageEarningsPerReferral": 75.50
                        },
                        "trends": {
                            "daily": [
                                {
                                    "date": "2024-03-15",
                                    "referrals": 12,
                                    "conversions": 5,
                                    "earnings": 375.00
                                },
                                {
                                    "date": "2024-03-16",
                                    "referrals": 15,
                                    "conversions": 6,
                                    "earnings": 450.00
                                }
                            ]
                        },
                        "topPerformers": [
                            {
                                "userId": 1,
                                "name": "John Doe",
                                "totalReferrals": 25,
                                "totalEarnings": 1875.00,
                                "conversionRate": 40.0
                            }
                        ]
                    }
                    """)))
    })
    @GetMapping("/referrals")
    public ResponseEntity<ReferralAnalyticsResponse> getReferralAnalytics(
            @Parameter(description = "Start date for analysis", example = "2024-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for analysis", example = "2024-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getReferralAnalytics(startDate, endDate));
    }

    @Operation(summary = "Get profit report", description = "Generate a detailed profit report including earnings breakdown and projections", responses = {
            @ApiResponse(responseCode = "200", description = "Profit report generated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfitReportResponse.class), examples = @ExampleObject(value = """
                    {
                        "summary": {
                            "totalRevenue": 25000.00,
                            "totalPayout": 2500.00,
                            "netProfit": 22500.00,
                            "profitMargin": 90.0
                        },
                        "breakdown": {
                            "directReferralPayouts": 2000.00,
                            "indirectReferralPayouts": 500.00,
                            "processingFees": 250.00,
                            "marketingCosts": 1000.00
                        },
                        "monthlyTrends": [
                            {
                                "month": "2024-03",
                                "revenue": 8500.00,
                                "payouts": 850.00,
                                "profit": 7650.00
                            }
                        ],
                        "projections": {
                            "nextMonth": {
                                "expectedRevenue": 9000.00,
                                "expectedPayouts": 900.00,
                                "expectedProfit": 8100.00
                            },
                            "nextQuarter": {
                                "expectedRevenue": 28000.00,
                                "expectedPayouts": 2800.00,
                                "expectedProfit": 25200.00
                            }
                        }
                    }
                    """)))
    })
    @GetMapping("/profit-report")
    public ResponseEntity<ProfitReportResponse> getProfitReport(
            @Parameter(description = "Start date for report", example = "2024-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for report", example = "2024-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getProfitReport(startDate, endDate));
    }

    @Operation(summary = "Export analytics data", description = "Export analytics data in CSV format with detailed metrics", responses = {
            @ApiResponse(responseCode = "200", description = "CSV file generated successfully", content = @Content(mediaType = "text/csv", examples = @ExampleObject(value = """
                    Date,Total Referrals,Active Referrers,Conversions,Revenue,Payouts,Profit
                    2024-03-15,12,8,5,2500.00,250.00,2250.00
                    2024-03-16,15,10,6,3000.00,300.00,2700.00
                    """)))
    })
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAnalytics(
            @Parameter(description = "Start date for export", example = "2024-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for export", example = "2024-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        byte[] csvData = analyticsService.exportAnalytics(startDate, endDate);
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=analytics_report.csv")
                .body(csvData);
    }
}