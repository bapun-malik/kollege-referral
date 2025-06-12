package com.kollege.referral.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = """
                Request object for user registration.
                A new user can be registered with or without a referral code.
                If a referral code is provided, the user will be linked to the referrer's network.
                """)
public class UserRegistrationRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Invalid email format")
        @Schema(description = "User's email address (must be unique)", example = "john.doe@example.com", required = true)
        private String email;

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        @Pattern(regexp = "^[\\p{L}\\s.'-]{2,50}$", message = "Name can only contain letters, spaces, dots, hyphens, and apostrophes")
        @Schema(description = "User's full name (2-50 characters)", example = "John Doe", required = true)
        private String name;

        @Pattern(regexp = "^[A-Z0-9]{8}$", message = "Referral code must be exactly 8 characters long and contain only uppercase letters and numbers")
        @Schema(description = """
                        Referral code of the referrer (optional).
                        - Must be exactly 8 characters long
                        - Can only contain uppercase letters (A-Z) and numbers (0-9)
                        - If provided, user will be added to referrer's network
                        - Referrer must have less than 8 direct referrals
                        """, example = "ABC123XY", required = false)
        private String referralCode;
}