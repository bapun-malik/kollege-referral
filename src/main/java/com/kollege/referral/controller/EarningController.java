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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
@SecurityRequirement(name = "bearer-jwt")
public class EarningController {

    private final EarningService earningService;

    @Operation(summary = "Get user's earnings summary", description = """
            Retrieve a comprehensive summary of user's earnings including:
            - Total earnings across all referral levels
            - Direct referral earnings (Level 1)
            - Indirect referral earnings (Level 2+)
            - Pending payouts
            - Monthly statistics and growth rate

            The response includes a breakdown of earnings and comparative statistics.
            """, responses = {
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
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "error": "Unauthorized",
                        "message": "Invalid or missing JWT token"
                    }
                    """))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User doesn't have permission to access this resource", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "error": "Forbidden",
                        "message": "Access denied to this resource"
                    }
                    """)))
    })
    @GetMapping("/{userId}/summary")
    public ResponseEntity<EarningsSummaryResponse> getEarningsSummary(
            @Parameter(description = "ID of the user", example = "1", in = ParameterIn.PATH, required = true) @PathVariable Long userId) {
        return ResponseEntity.ok(earningService.getEarningsSummary(userId));
    }

    @Operation(summary = "Get detailed earnings history", description = """
            Retrieve detailed earnings history with optional date range filter.
            The history includes:
            - Individual earning entries
            - Referral level information
            - Referred user details
            - Purchase amounts
            - Timestamps

            Use date parameters to filter the results within a specific time range.
            """, responses = {
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
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid date range", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "error": "Invalid date range",
                        "message": "End date cannot be before start date"
                    }
                    """)))
    })
    @GetMapping("/{userId}/history")
    public ResponseEntity<List<EarningResponse>> getEarningsHistory(
            @Parameter(description = "ID of the user", example = "1", in = ParameterIn.PATH, required = true) @PathVariable Long userId,
            @Parameter(description = "Start date (inclusive)", example = "2024-03-01", in = ParameterIn.QUERY) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive)", example = "2024-03-31", in = ParameterIn.QUERY) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(earningService.getEarningsHistory(userId, startDate, endDate));
    }

    @Operation(summary = "Export earnings report", description = """
            Export earnings data in CSV format with optional date range filter.
            The CSV report includes:
            - Transaction dates
            - Earning amounts
            - Referral levels
            - Referred user information
            - Purchase amounts

            The exported file is in CSV format and can be opened in spreadsheet applications.
            """, responses = {
            @ApiResponse(responseCode = "200", description = "CSV file generated successfully", content = @Content(mediaType = "text/csv", examples = @ExampleObject(value = """
                    Date,Amount,Level,Referred User,Purchase Amount
                    2024-03-15 10:30:00,100.00,1,Jane Smith,1000.00
                    2024-03-16 14:20:00,50.00,2,Bob Wilson,1000.00
                    """))),
            @ApiResponse(responseCode = "500", description = "Error generating CSV file", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "error": "Export failed",
                        "message": "Unable to generate CSV file"
                    }
                    """)))
    })
    @GetMapping("/{userId}/export")
    public ResponseEntity<byte[]> exportEarnings(
            @Parameter(description = "ID of the user", example = "1", in = ParameterIn.PATH, required = true) @PathVariable Long userId,
            @Parameter(description = "Start date (inclusive)", example = "2024-03-01", in = ParameterIn.QUERY) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive)", example = "2024-03-31", in = ParameterIn.QUERY) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        byte[] csvData = earningService.exportEarnings(userId, startDate, endDate);
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=earnings_report.csv")
                .body(csvData);
    }
}