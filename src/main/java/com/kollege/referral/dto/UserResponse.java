package com.kollege.referral.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

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
}