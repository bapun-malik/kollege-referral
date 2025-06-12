package com.kollege.referral.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Response object containing user details")
public class UserResponse {

    @Schema(description = "User's unique identifier", example = "1")
    private Long id;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User's full name", example = "John Doe")
    private String name;

    @Schema(description = "User's unique referral code", example = "XYZ789AB")
    private String referralCode;

    @Schema(description = "Full referral link for sharing", example = "https://kollege.com/ref/XYZ789AB")
    private String referralLink;

    @Schema(description = "User's Gravatar URL or default avatar", example = "https://www.gravatar.com/avatar/abc123...?d=identicon&s=200")
    private String avatarUrl;

    @Schema(description = "Total earnings from all referrals", example = "1250.00")
    private double totalEarnings;

    @Schema(description = "Earnings from direct referrals", example = "1000.00")
    private double directEarnings;

    @Schema(description = "Earnings from indirect referrals", example = "250.00")
    private double indirectEarnings;

    @Schema(description = "User's current referral level (1-8)", example = "2")
    private int referralLevel;

    @Schema(description = "Number of direct referrals", example = "3")
    private int directReferralsCount;

    @Schema(description = "Whether the user account is active", example = "true")
    private boolean active;

    @Schema(description = "Account creation timestamp", example = "2024-03-20T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last activity timestamp", example = "2024-03-21T15:45:00")
    private LocalDateTime lastActive;

    @Schema(description = "Parent referrer's ID (null if no referrer)", example = "5")
    private Long parentUserId;

    @Schema(description = "Whether user can accept more referrals (max 8)", example = "true")
    private boolean canAcceptMoreReferrals;
}