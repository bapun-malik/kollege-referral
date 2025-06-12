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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user registration and referral management")
public class UserController {

        private final UserService userService;

        @Operation(summary = "Register a new user", description = "Register a new user with optional referral code. If referral code is provided, the user will be linked to the referrer.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserRegistrationRequest.class), examples = @ExampleObject(value = """
                        {
                            "email": "john.doe@example.com",
                            "password": "SecurePass123!",
                            "name": "John Doe",
                            "referralCode": "ABC123XY"
                        }
                        """))), responses = {
                        @ApiResponse(responseCode = "201", description = "User successfully registered", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class), examples = @ExampleObject(value = """
                                        {
                                            "id": 1,
                                            "email": "john.doe@example.com",
                                            "name": "John Doe",
                                            "referralCode": "XYZ789AB",
                                            "referralLink": "https://kollege.com/ref/XYZ789AB"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data or referral code", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                        {
                                            "error": "Invalid referral code",
                                            "message": "The provided referral code does not exist"
                                        }
                                        """)))
        })
        @PostMapping("/register")
        public ResponseEntity<UserResponse> registerUser(
                        @Valid @RequestBody UserRegistrationRequest request) {
                return ResponseEntity.ok(userService.registerUser(request));
        }

        @Operation(summary = "Get user's referral tree", description = "Retrieve the complete referral tree for a user, including direct and indirect referrals", responses = {
                        @ApiResponse(responseCode = "200", description = "Referral tree retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReferralTreeResponse.class), examples = @ExampleObject(value = """
                                        {
                                            "userId": 1,
                                            "directReferrals": {
                                                "count": 2,
                                                "users": [
                                                    {
                                                        "id": 2,
                                                        "name": "Jane Smith",
                                                        "registrationDate": "2024-03-15T10:30:00Z",
                                                        "totalPurchases": 500.00
                                                    },
                                                    {
                                                        "id": 3,
                                                        "name": "Bob Wilson",
                                                        "registrationDate": "2024-03-16T14:20:00Z",
                                                        "totalPurchases": 750.00
                                                    }
                                                ]
                                            },
                                            "indirectReferrals": {
                                                "count": 1,
                                                "users": [
                                                    {
                                                        "id": 4,
                                                        "name": "Alice Brown",
                                                        "registrationDate": "2024-03-17T09:15:00Z",
                                                        "totalPurchases": 300.00
                                                    }
                                                ]
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
        @GetMapping("/{userId}/referrals")
        public ResponseEntity<ReferralTreeResponse> getReferralTree(
                        @Parameter(description = "ID of the user", example = "1") @PathVariable Long userId) {
                return ResponseEntity.ok(userService.getReferralTree(userId));
        }

        @Operation(summary = "Generate new referral code", description = "Generate a new referral code for the user. The old referral code will be invalidated.", responses = {
                        @ApiResponse(responseCode = "200", description = "New referral code generated successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                                        {
                                            "referralCode": "NEW789XY",
                                            "referralLink": "https://kollege.com/ref/NEW789XY"
                                        }
                                        """)))
        })
        @PostMapping("/{userId}/referral-code")
        public ResponseEntity<?> generateNewReferralCode(
                        @Parameter(description = "ID of the user", example = "1") @PathVariable Long userId) {
                return ResponseEntity.ok(userService.generateNewReferralCode(userId));
        }
}