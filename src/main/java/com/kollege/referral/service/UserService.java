package com.kollege.referral.service;

import com.kollege.referral.dto.UserRegistrationRequest;
import com.kollege.referral.dto.UserResponse;
import com.kollege.referral.dto.ReferralTreeResponse;

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
}