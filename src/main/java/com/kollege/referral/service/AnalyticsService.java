package com.kollege.referral.service;

import com.kollege.referral.dto.ReferralAnalyticsResponse;
import com.kollege.referral.dto.ProfitReportResponse;
import java.time.LocalDate;

public interface AnalyticsService {

    /**
     * Get comprehensive referral analytics
     *
     * @param startDate Optional start date for filtering
     * @param endDate   Optional end date for filtering
     * @return Analytics data including trends and top performers
     */
    ReferralAnalyticsResponse getReferralAnalytics(LocalDate startDate, LocalDate endDate);

    /**
     * Generate profit report
     *
     * @param startDate Optional start date for filtering
     * @param endDate   Optional end date for filtering
     * @return Detailed profit report with projections
     */
    ProfitReportResponse getProfitReport(LocalDate startDate, LocalDate endDate);

    /**
     * Export referral analytics data as CSV
     * 
     * @param startDate Start date for the export period
     * @param endDate   End date for the export period
     * @return CSV data as byte array
     */
    byte[] exportReferralAnalytics(LocalDate startDate, LocalDate endDate);

    /**
     * Export profit report data as CSV
     * 
     * @param startDate Start date for the export period
     * @param endDate   End date for the export period
     * @return CSV data as byte array
     */
    byte[] exportProfitReport(LocalDate startDate, LocalDate endDate);
}