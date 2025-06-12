package com.kollege.referral.service;

import com.kollege.referral.dto.UserRegistrationRequest;
import com.kollege.referral.dto.UserResponse;
import com.kollege.referral.dto.ReferralTreeResponse;
import java.time.LocalDate;

public interface UserService {

    /**
     * Register a new user with optional referral code
     *
     * @param request Registration request containing user details and optional
     *                referral code
     * @return Registered user details
     */
    UserResponse registerUser(UserRegistrationRequest request);

    /**
     * Get the referral tree for a user
     *
     * @param userId ID of the user
     * @return Referral tree containing direct and indirect referrals
     */
    ReferralTreeResponse getReferralTree(Long userId);

    /**
     * Generate a new referral code for a user
     *
     * @param userId ID of the user
     * @return New referral code details
     */
    Object generateNewReferralCode(Long userId);

    /**
     * Export all users data as CSV
     * 
     * @return CSV data as byte array
     */
    byte[] exportUsers();

    /**
     * Export specific user's detailed data including referrals and earnings
     * 
     * @param userId    The ID of the user
     * @param startDate Optional start date for filtering earnings data
     * @param endDate   Optional end date for filtering earnings data
     * @return CSV data as byte array
     */
    byte[] exportUserDetails(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Export referral network data for a user
     * 
     * @param userId The ID of the user
     * @return CSV data as byte array
     */
    byte[] exportUserReferralNetwork(Long userId);
}