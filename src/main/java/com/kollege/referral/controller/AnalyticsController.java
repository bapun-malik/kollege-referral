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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "APIs for analytics and reporting")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get referral analytics", description = "Retrieve referral analytics with optional date range filter")
    @GetMapping("/referrals")
    public ResponseEntity<ReferralAnalyticsResponse> getReferralAnalytics(
            @Parameter(description = "Start date (inclusive)", example = "2024-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive)", example = "2024-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getReferralAnalytics(startDate, endDate));
    }

    @Operation(summary = "Get profit report", description = "Retrieve profit report with optional date range filter")
    @GetMapping("/profit")
    public ResponseEntity<ProfitReportResponse> getProfitReport(
            @Parameter(description = "Start date (inclusive)", example = "2024-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive)", example = "2024-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getProfitReport(startDate, endDate));
    }

    @Operation(summary = "Export referral analytics", description = "Export referral analytics data as CSV")
    @GetMapping("/referrals/export")
    public ResponseEntity<byte[]> exportReferralAnalytics(
            @Parameter(description = "Start date (inclusive)", example = "2024-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive)", example = "2024-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] csvData = analyticsService.exportReferralAnalytics(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "referral-analytics.csv");
        headers.setContentLength(csvData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    @Operation(summary = "Export profit report", description = "Export profit report data as CSV")
    @GetMapping("/profit/export")
    public ResponseEntity<byte[]> exportProfitReport(
            @Parameter(description = "Start date (inclusive)", example = "2024-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive)", example = "2024-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] csvData = analyticsService.exportProfitReport(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "profit-report.csv");
        headers.setContentLength(csvData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }
}