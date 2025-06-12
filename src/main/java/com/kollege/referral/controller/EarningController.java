package com.kollege.referral.controller;

import com.kollege.referral.dto.EarningResponse;
import com.kollege.referral.dto.EarningsSummaryResponse;
import com.kollege.referral.service.EarningService;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/earnings")
@RequiredArgsConstructor
@Tag(name = "Earnings Management", description = "APIs for managing and retrieving earning information")
public class EarningController {

        private final EarningService earningService;

        @Operation(summary = "Get user's earnings summary", description = "Retrieve a summary of user's earnings including total, direct, and indirect earnings", responses = {
                        @ApiResponse(responseCode = "200", description = "Earnings summary retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EarningsSummaryResponse.class), examples = @ExampleObject(value = """
                                        {
                                            "totalEarnings": 1250.00,
                                            "directReferralEarnings": 1000.00,
                                            "indirectReferralEarnings": 250.00,
                                            "pendingPayouts": 100.00,
                                            "monthlyStats": {
                                                "currentMonth": 450.00,
                                                "previousMonth": 800.00,
                                                "monthlyGrowth": -43.75
                                            }
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                        {
                                            "error": "User not found",
                                            "message": "No user found with ID 1"
                                        }
                                        """)))
        })
        @GetMapping("/{userId}/summary")
        public ResponseEntity<EarningsSummaryResponse> getEarningsSummary(
                        @Parameter(description = "ID of the user", example = "1") @PathVariable Long userId) {
                return ResponseEntity.ok(earningService.getEarningsSummary(userId));
        }

        @Operation(summary = "Get detailed earnings history", description = "Retrieve detailed earnings history with optional date range filter", responses = {
                        @ApiResponse(responseCode = "200", description = "Earnings history retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EarningResponse.class), examples = @ExampleObject(value = """
                                        {
                                            "earnings": [
                                                {
                                                    "id": 1,
                                                    "amount": 100.00,
                                                    "referralLevel": 1,
                                                    "timestamp": "2024-03-15T10:30:00Z",
                                                    "referredUser": {
                                                        "id": 2,
                                                        "name": "Jane Smith",
                                                        "purchaseAmount": 1000.00
                                                    }
                                                },
                                                {
                                                    "id": 2,
                                                    "amount": 50.00,
                                                    "referralLevel": 2,
                                                    "timestamp": "2024-03-16T14:20:00Z",
                                                    "referredUser": {
                                                        "id": 3,
                                                        "name": "Bob Wilson",
                                                        "purchaseAmount": 1000.00
                                                    }
                                                }
                                            ],
                                            "totalCount": 2,
                                            "totalAmount": 150.00
                                        }
                                        """)))
        })
        @GetMapping("/{userId}/history")
        public ResponseEntity<List<EarningResponse>> getEarningsHistory(
                        @Parameter(description = "ID of the user", example = "1") @PathVariable Long userId,
                        @Parameter(description = "Start date (inclusive)", example = "2024-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @Parameter(description = "End date (inclusive)", example = "2024-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
                return ResponseEntity.ok(earningService.getEarningsHistory(userId, startDate, endDate));
        }

        @Operation(summary = "Export earnings report", description = "Export earnings data in CSV format with optional date range filter", responses = {
                        @ApiResponse(responseCode = "200", description = "CSV file generated successfully", content = @Content(mediaType = "text/csv", examples = @ExampleObject(value = """
                                        Date,Amount,Level,Referred User,Purchase Amount
                                        2024-03-15 10:30:00,100.00,1,Jane Smith,1000.00
                                        2024-03-16 14:20:00,50.00,2,Bob Wilson,1000.00
                                        """)))
        })
        @GetMapping("/{userId}/export")
        public ResponseEntity<byte[]> exportEarnings(
                        @Parameter(description = "ID of the user", example = "1") @PathVariable Long userId,
                        @Parameter(description = "Start date (inclusive)", example = "2024-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @Parameter(description = "End date (inclusive)", example = "2024-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
                byte[] csvData = earningService.exportEarnings(userId, startDate, endDate);
                return ResponseEntity.ok()
                                .header("Content-Type", "text/csv")
                                .header("Content-Disposition", "attachment; filename=earnings_report.csv")
                                .body(csvData);
        }
}