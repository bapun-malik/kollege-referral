package com.kollege.referral.controller;

import com.kollege.referral.dto.UserRegistrationRequest;
import com.kollege.referral.dto.UserResponse;
import com.kollege.referral.dto.ReferralTreeResponse;
import com.kollege.referral.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users, referrals, and exports")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Register a new user", description = """
            Register a new user in the referral system.

            Features:
            - Automatic unique referral code generation
            - Optional referral linking
            - Gravatar integration for avatars
            - Maximum 8 direct referrals per user

            The system will:
            1. Validate email uniqueness
            2. Generate a unique referral code
            3. Set up referral relationship if code provided
            4. Create Gravatar-based avatar
            """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserRegistrationRequest.class), examples = @ExampleObject(name = "New User Registration", summary = "Register a new user with referral", value = """
            {
                "email": "sarah.smith@example.com",
                "name": "Sarah Smith",
                "referralCode": "XYZ789AB"
            }
            """))), responses = {
            @ApiResponse(responseCode = "201", description = "User successfully registered", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class), examples = @ExampleObject(value = """
                    {
                        "id": 1,
                        "email": "sarah.smith@example.com",
                        "name": "Sarah Smith",
                        "referralCode": "ABC123XY",
                        "referralLink": "https://kollege.com/ref/ABC123XY",
                        "avatarUrl": "https://www.gravatar.com/avatar/abc123...?d=identicon&s=200",
                        "totalEarnings": 0.00,
                        "directEarnings": 0.00,
                        "indirectEarnings": 0.00,
                        "referralLevel": 1,
                        "directReferralsCount": 0,
                        "active": true,
                        "createdAt": "2024-03-20T10:30:00",
                        "lastActive": "2024-03-20T10:30:00",
                        "parentUserId": 5,
                        "canAcceptMoreReferrals": true
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-03-20T10:30:00Z",
                        "status": 400,
                        "error": "Bad Request",
                        "messages": {
                            "email": "Email is already registered",
                            "name": "Name must be between 2 and 50 characters",
                            "referralCode": "Invalid referral code format"
                        }
                    }
                    """)))
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
    }

    @Operation(summary = "Get user's referral tree", description = """
            Retrieve the complete referral tree for a user, including:
            - Direct referrals (Level 1)
            - Indirect referrals (Level 2)
            - Referral statistics
            - Earnings information
            - Registration dates
            """, responses = {
            @ApiResponse(responseCode = "200", description = "Referral tree retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReferralTreeResponse.class), examples = @ExampleObject(value = """
                    {
                        "userId": 1,
                        "directReferrals": {
                            "count": 3,
                            "users": [
                                {
                                    "id": 2,
                                    "name": "Jane Smith",
                                    "registrationDate": "2024-03-15T10:30:00Z",
                                    "totalPurchases": 1500.00,
                                    "status": "ACTIVE",
                                    "referralCode": "DEF456GH",
                                    "directReferralsCount": 2
                                },
                                {
                                    "id": 3,
                                    "name": "Bob Wilson",
                                    "registrationDate": "2024-03-16T14:20:00Z",
                                    "totalPurchases": 2750.00,
                                    "status": "ACTIVE",
                                    "referralCode": "IJK789LM",
                                    "directReferralsCount": 1
                                },
                                {
                                    "id": 4,
                                    "name": "Alice Brown",
                                    "registrationDate": "2024-03-17T09:15:00Z",
                                    "totalPurchases": 950.00,
                                    "status": "ACTIVE",
                                    "referralCode": "NOP123QR",
                                    "directReferralsCount": 0
                                }
                            ]
                        },
                        "indirectReferrals": {
                            "count": 3,
                            "users": [
                                {
                                    "id": 5,
                                    "name": "Charlie Davis",
                                    "registrationDate": "2024-03-18T11:45:00Z",
                                    "totalPurchases": 800.00,
                                    "status": "ACTIVE",
                                    "referralCode": "STU456VW",
                                    "parentReferrer": "Jane Smith"
                                },
                                {
                                    "id": 6,
                                    "name": "Emma Wilson",
                                    "registrationDate": "2024-03-19T16:30:00Z",
                                    "totalPurchases": 1200.00,
                                    "status": "ACTIVE",
                                    "referralCode": "XYZ789AB",
                                    "parentReferrer": "Jane Smith"
                                },
                                {
                                    "id": 7,
                                    "name": "David Lee",
                                    "registrationDate": "2024-03-20T13:20:00Z",
                                    "totalPurchases": 650.00,
                                    "status": "ACTIVE",
                                    "referralCode": "CDE123FG",
                                    "parentReferrer": "Bob Wilson"
                                }
                            ]
                        },
                        "statistics": {
                            "totalNetworkSize": 6,
                            "totalNetworkEarnings": 7850.00,
                            "averageEarningsPerReferral": 1308.33,
                            "mostActiveReferrer": "Jane Smith",
                            "topEarner": "Bob Wilson"
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-03-20T10:30:00Z",
                        "status": 404,
                        "error": "Not Found",
                        "message": "User with ID 1 not found"
                    }
                    """)))
    })
    @GetMapping("/{userId}/referrals")
    public ResponseEntity<ReferralTreeResponse> getReferralTree(
            @Parameter(description = "ID of the user", example = "1") @PathVariable Long userId) {
        return ResponseEntity.ok(userService.getReferralTree(userId));
    }

    @Operation(summary = "Generate new referral code", description = """
            Generate a new referral code for the user.
            - Old referral code will be invalidated
            - Existing referral relationships are preserved
            - New referral link is generated
            """, responses = {
            @ApiResponse(responseCode = "200", description = "New referral code generated successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "referralCode": "HIJ456KL",
                        "referralLink": "https://kollege.com/ref/HIJ456KL",
                        "generatedAt": "2024-03-20T10:30:00Z",
                        "expiresAt": null,
                        "status": "ACTIVE"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-03-20T10:30:00Z",
                        "status": 404,
                        "error": "Not Found",
                        "message": "User with ID 1 not found"
                    }
                    """)))
    })
    @PostMapping("/{userId}/referral-code")
    public ResponseEntity<?> generateNewReferralCode(
            @Parameter(description = "ID of the user", example = "1") @PathVariable Long userId) {
        return ResponseEntity.ok(userService.generateNewReferralCode(userId));
    }

    @Operation(summary = "Export all users", description = """
            Export all users data as CSV.
            Includes:
            - User details
            - Referral information
            - Earnings data
            - Activity timestamps
            """, responses = {
            @ApiResponse(responseCode = "200", description = "CSV file generated successfully", content = @Content(mediaType = "text/csv", examples = @ExampleObject(value = """
                    User ID,Name,Email,Referral Code,Total Earnings,Direct Earnings,Indirect Earnings,Direct Referrals,Indirect Referrals,Created At,Last Active
                    1,Sarah Smith,sarah.smith@example.com,ABC123XY,5000.00,3500.00,1500.00,3,3,2024-03-15 10:30:00,2024-03-20 15:45:00
                    2,Jane Wilson,jane.wilson@example.com,DEF456GH,2500.00,2000.00,500.00,2,1,2024-03-16 14:20:00,2024-03-20 16:30:00
                    3,Bob Brown,bob.brown@example.com,IJK789LM,1800.00,1800.00,0.00,1,0,2024-03-17 09:15:00,2024-03-20 12:20:00
                    """)))
    })
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportUsers() {
        byte[] csvData = userService.exportUsers();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv")
                .body(csvData);
    }

    @Operation(summary = "Export user details", description = """
            Export detailed information about a specific user including:
            - Personal information
            - Referral network
            - Earnings history
            - Activity timeline

            Optional date range filter for earnings data.
            """, responses = {
            @ApiResponse(responseCode = "200", description = "CSV file generated successfully", content = @Content(mediaType = "text/csv", examples = @ExampleObject(value = """
                    User Profile
                    ID,Name,Email,Referral Code,Created At,Last Active
                    1,Sarah Smith,sarah.smith@example.com,ABC123XY,2024-03-15 10:30:00,2024-03-20 15:45:00

                    Earnings Summary
                    Total Earnings,Direct Earnings,Indirect Earnings
                    5000.00,3500.00,1500.00

                    Detailed Earnings
                    Date,Amount,Level,Source User,Transaction Amount
                    2024-03-16 14:20:00,350.00,1,Jane Wilson,3500.00
                    2024-03-17 09:15:00,180.00,1,Bob Brown,1800.00
                    2024-03-18 11:45:00,120.00,2,Charlie Davis,1200.00
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-03-20T10:30:00Z",
                        "status": 404,
                        "error": "Not Found",
                        "message": "User with ID 1 not found"
                    }
                    """)))
    })
    @GetMapping("/{userId}/export")
    public ResponseEntity<byte[]> exportUserDetails(
            @Parameter(description = "ID of the user", example = "1") @PathVariable Long userId,

            @Parameter(description = "Start date (inclusive)", example = "2024-03-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "End date (inclusive)", example = "2024-03-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        byte[] csvData = userService.exportUserDetails(userId, startDate, endDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user-" + userId + "-details.csv")
                .body(csvData);
    }

    @Operation(summary = "Export user referral network", description = """
            Export user's complete referral network data including:
            - Direct referrals (Level 1)
            - Indirect referrals (Level 2)
            - Network statistics
            - Earnings breakdown
            """, responses = {
            @ApiResponse(responseCode = "200", description = "CSV file generated successfully", content = @Content(mediaType = "text/csv", examples = @ExampleObject(value = """
                    Direct Referrals (Level 1)
                    ID,Name,Email,Referral Code,Total Earnings,Created At,Last Active
                    2,Jane Wilson,jane.wilson@example.com,DEF456GH,2500.00,2024-03-16 14:20:00,2024-03-20 16:30:00
                    3,Bob Brown,bob.brown@example.com,IJK789LM,1800.00,2024-03-17 09:15:00,2024-03-20 12:20:00

                    Indirect Referrals (Level 2)
                    ID,Name,Email,Referral Code,Total Earnings,Created At,Last Active,Parent Referrer
                    4,Charlie Davis,charlie.davis@example.com,MNO123PQ,1200.00,2024-03-18 11:45:00,2024-03-20 14:15:00,Jane Wilson
                    5,David Lee,david.lee@example.com,RST456UV,950.00,2024-03-19 10:30:00,2024-03-20 15:45:00,Bob Brown

                    Network Statistics
                    Metric,Value
                    Total Network Size,4
                    Direct Referrals,2
                    Indirect Referrals,2
                    Network Total Earnings,6450.00
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-03-20T10:30:00Z",
                        "status": 404,
                        "error": "Not Found",
                        "message": "User with ID 1 not found"
                    }
                    """)))
    })
    @GetMapping("/{userId}/network/export")
    public ResponseEntity<byte[]> exportUserReferralNetwork(
            @Parameter(description = "ID of the user", example = "1") @PathVariable Long userId) {
        byte[] csvData = userService.exportUserReferralNetwork(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user-" + userId + "-network.csv")
                .body(csvData);
    }
}