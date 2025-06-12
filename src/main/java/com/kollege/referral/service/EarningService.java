package com.kollege.referral.service;

import com.kollege.referral.dto.EarningResponse;
import com.kollege.referral.dto.EarningsSummaryResponse;
import java.time.LocalDate;
import java.util.List;

public interface EarningService {

    /**
     * Get earnings summary for a user
     *
     * @param userId The ID of the user
     * @return Summary of user's earnings
     */
    EarningsSummaryResponse getEarningsSummary(Long userId);

    /**
     * Get detailed earnings history for a user
     *
     * @param userId    The ID of the user
     * @param startDate Optional start date for filtering
     * @param endDate   Optional end date for filtering
     * @return List of earnings
     */
    List<EarningResponse> getEarningsHistory(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Export earnings data as CSV
     *
     * @param userId    The ID of the user
     * @param startDate Optional start date for filtering
     * @param endDate   Optional end date for filtering
     * @return CSV data as byte array
     */
    byte[] exportEarnings(Long userId, LocalDate startDate, LocalDate endDate);
}