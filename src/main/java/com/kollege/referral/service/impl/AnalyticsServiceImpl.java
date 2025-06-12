package com.kollege.referral.service.impl;

import com.kollege.referral.dto.ReferralAnalyticsResponse;
import com.kollege.referral.dto.ProfitReportResponse;
import com.kollege.referral.service.AnalyticsService;
import com.kollege.referral.repository.UserRepository;
import com.kollege.referral.repository.EarningRepository;
import com.kollege.referral.repository.TransactionRepository;
import com.kollege.referral.model.User;
import com.kollege.referral.model.Earning;
import com.kollege.referral.model.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

        private final UserRepository userRepository;
        private final EarningRepository earningRepository;
        private final TransactionRepository transactionRepository;

        @Override
        public ReferralAnalyticsResponse getReferralAnalytics(LocalDate startDate, LocalDate endDate) {
                LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
                LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

                // Get total referrals and active referrers
                long totalReferrals = userRepository.count() - 1; // Excluding root user
                long activeReferrers = userRepository.findAll().stream()
                                .filter(user -> !user.getDirectReferrals().isEmpty())
                                .count();

                // Calculate conversion rate
                double conversionRate = (activeReferrers / (double) totalReferrals) * 100;

                // Calculate average earnings per referral
                List<Earning> allEarnings = earningRepository.findByTimestampBetween(start, end, null).getContent();
                double totalEarnings = allEarnings.stream().mapToDouble(Earning::getEarnedAmount).sum();
                double avgEarningsPerReferral = totalReferrals > 0 ? totalEarnings / totalReferrals : 0;

                // Get daily trends
                List<ReferralAnalyticsResponse.DailyTrend> dailyTrends = new ArrayList<>();
                LocalDate currentDate = start.toLocalDate();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                while (!currentDate.isAfter(end.toLocalDate())) {
                        LocalDateTime dayStart = currentDate.atStartOfDay();
                        LocalDateTime dayEnd = currentDate.atTime(23, 59, 59);

                        long dailyReferrals = userRepository.findAll().stream()
                                        .filter(user -> user.getCreatedAt().isAfter(dayStart)
                                                        && user.getCreatedAt().isBefore(dayEnd))
                                        .count();

                        List<Earning> dailyEarnings = earningRepository.findByTimestampBetween(dayStart, dayEnd, null)
                                        .getContent();
                        double dailyEarningsAmount = dailyEarnings.stream().mapToDouble(Earning::getEarnedAmount).sum();

                        dailyTrends.add(ReferralAnalyticsResponse.DailyTrend.builder()
                                        .date(currentDate.format(dateFormatter))
                                        .referrals((int) dailyReferrals)
                                        .conversions((int) (dailyReferrals * conversionRate / 100))
                                        .earnings(dailyEarningsAmount)
                                        .build());

                        currentDate = currentDate.plusDays(1);
                }

                // Get top performers
                List<ReferralAnalyticsResponse.TopPerformer> topPerformers = userRepository.findAll().stream()
                                .filter(user -> !user.getDirectReferrals().isEmpty())
                                .map(user -> {
                                        double userTotalEarnings = user.getTotalEarnings();
                                        long userReferrals = user.getDirectReferrals().size();
                                        double userConversionRate = (userReferrals / (double) totalReferrals) * 100;

                                        return ReferralAnalyticsResponse.TopPerformer.builder()
                                                        .userId(user.getId())
                                                        .name(user.getName())
                                                        .totalReferrals((int) userReferrals)
                                                        .totalEarnings(userTotalEarnings)
                                                        .conversionRate(userConversionRate)
                                                        .build();
                                })
                                .sorted((a, b) -> Double.compare(b.getTotalEarnings(), a.getTotalEarnings()))
                                .limit(5)
                                .collect(Collectors.toList());

                return ReferralAnalyticsResponse.builder()
                                .overview(ReferralAnalyticsResponse.Overview.builder()
                                                .totalReferrals((int) totalReferrals)
                                                .activeReferrers((int) activeReferrers)
                                                .conversionRate(conversionRate)
                                                .averageEarningsPerReferral(avgEarningsPerReferral)
                                                .build())
                                .trends(ReferralAnalyticsResponse.Trends.builder()
                                                .daily(dailyTrends)
                                                .build())
                                .topPerformers(topPerformers)
                                .build();
        }

        @Override
        public ProfitReportResponse getProfitReport(LocalDate startDate, LocalDate endDate) {
                LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
                LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

                // Calculate revenue (total transaction amounts)
                List<Transaction> transactions = transactionRepository.findAll().stream()
                                .filter(t -> t.getTimestamp().isAfter(start) && t.getTimestamp().isBefore(end))
                                .collect(Collectors.toList());
                double totalRevenue = transactions.stream().mapToDouble(Transaction::getAmount).sum();

                // Calculate payouts (total earnings)
                List<Earning> earnings = earningRepository.findByTimestampBetween(start, end, null).getContent();
                double totalPayout = earnings.stream().mapToDouble(Earning::getEarnedAmount).sum();

                // Calculate breakdown
                double directPayouts = earnings.stream()
                                .filter(e -> e.getLevel() == 1)
                                .mapToDouble(Earning::getEarnedAmount)
                                .sum();
                double indirectPayouts = earnings.stream()
                                .filter(e -> e.getLevel() == 2)
                                .mapToDouble(Earning::getEarnedAmount)
                                .sum();

                // Assume processing fees are 1% of revenue and marketing costs are 5% of
                // revenue
                double processingFees = totalRevenue * 0.01;
                double marketingCosts = totalRevenue * 0.05;

                // Calculate net profit and margin
                double netProfit = totalRevenue - totalPayout - processingFees - marketingCosts;
                double profitMargin = totalRevenue > 0 ? (netProfit / totalRevenue) * 100 : 0;

                // Get monthly trends
                List<ProfitReportResponse.MonthlyTrend> monthlyTrends = new ArrayList<>();
                LocalDate currentMonth = start.toLocalDate().withDayOfMonth(1);
                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

                while (!currentMonth.isAfter(end.toLocalDate())) {
                        LocalDateTime monthStart = currentMonth.atStartOfDay();
                        LocalDateTime monthEnd = currentMonth.plusMonths(1).atStartOfDay().minusSeconds(1);

                        double monthRevenue = transactions.stream()
                                        .filter(t -> t.getTimestamp().isAfter(monthStart)
                                                        && t.getTimestamp().isBefore(monthEnd))
                                        .mapToDouble(Transaction::getAmount)
                                        .sum();

                        double monthPayouts = earnings.stream()
                                        .filter(e -> e.getTimestamp().isAfter(monthStart)
                                                        && e.getTimestamp().isBefore(monthEnd))
                                        .mapToDouble(Earning::getEarnedAmount)
                                        .sum();

                        double monthProfit = monthRevenue - monthPayouts - (monthRevenue * 0.06); // 6% for fees and
                                                                                                  // marketing

                        monthlyTrends.add(ProfitReportResponse.MonthlyTrend.builder()
                                        .month(currentMonth.format(monthFormatter))
                                        .revenue(monthRevenue)
                                        .payouts(monthPayouts)
                                        .profit(monthProfit)
                                        .build());

                        currentMonth = currentMonth.plusMonths(1);
                }

                // Calculate projections based on current trends
                double avgMonthlyRevenue = totalRevenue / monthlyTrends.size();
                double avgMonthlyPayout = totalPayout / monthlyTrends.size();
                double avgMonthlyProfit = netProfit / monthlyTrends.size();

                // Apply growth factor (assume 10% monthly growth)
                double growthFactor = 1.10;

                return ProfitReportResponse.builder()
                                .summary(ProfitReportResponse.Summary.builder()
                                                .totalRevenue(totalRevenue)
                                                .totalPayout(totalPayout)
                                                .netProfit(netProfit)
                                                .profitMargin(profitMargin)
                                                .build())
                                .breakdown(ProfitReportResponse.Breakdown.builder()
                                                .directReferralPayouts(directPayouts)
                                                .indirectReferralPayouts(indirectPayouts)
                                                .processingFees(processingFees)
                                                .marketingCosts(marketingCosts)
                                                .build())
                                .monthlyTrends(monthlyTrends)
                                .projections(ProfitReportResponse.Projections.builder()
                                                .nextMonth(ProfitReportResponse.Projection.builder()
                                                                .expectedRevenue(avgMonthlyRevenue * growthFactor)
                                                                .expectedPayouts(avgMonthlyPayout * growthFactor)
                                                                .expectedProfit(avgMonthlyProfit * growthFactor)
                                                                .build())
                                                .nextQuarter(ProfitReportResponse.Projection.builder()
                                                                .expectedRevenue(avgMonthlyRevenue * growthFactor * 3)
                                                                .expectedPayouts(avgMonthlyPayout * growthFactor * 3)
                                                                .expectedProfit(avgMonthlyProfit * growthFactor * 3)
                                                                .build())
                                                .build())
                                .build();
        }

        @Override
        public byte[] exportAnalytics(LocalDate startDate, LocalDate endDate) {
                LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
                LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

                StringBuilder csv = new StringBuilder();
                csv.append("Date,Total Referrals,Active Referrers,Conversions,Revenue,Payouts,Profit\n");

                LocalDate currentDate = start.toLocalDate();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                while (!currentDate.isAfter(end.toLocalDate())) {
                        LocalDateTime dayStart = currentDate.atStartOfDay();
                        LocalDateTime dayEnd = currentDate.atTime(23, 59, 59);

                        // Get daily metrics
                        long dailyReferrals = userRepository.findAll().stream()
                                        .filter(user -> user.getCreatedAt().isAfter(dayStart)
                                                        && user.getCreatedAt().isBefore(dayEnd))
                                        .count();

                        long activeReferrers = userRepository.findAll().stream()
                                        .filter(user -> !user.getDirectReferrals().isEmpty()
                                                        && user.getLastActive().isAfter(dayStart)
                                                        && user.getLastActive().isBefore(dayEnd))
                                        .count();

                        List<Transaction> dailyTransactions = transactionRepository.findAll().stream()
                                        .filter(t -> t.getTimestamp().isAfter(dayStart)
                                                        && t.getTimestamp().isBefore(dayEnd))
                                        .collect(Collectors.toList());
                        double dailyRevenue = dailyTransactions.stream().mapToDouble(Transaction::getAmount).sum();

                        List<Earning> dailyEarnings = earningRepository.findByTimestampBetween(dayStart, dayEnd, null)
                                        .getContent();
                        double dailyPayouts = dailyEarnings.stream().mapToDouble(Earning::getEarnedAmount).sum();

                        double dailyProfit = dailyRevenue - dailyPayouts - (dailyRevenue * 0.06); // 6% for fees and
                                                                                                  // marketing

                        // Add row to CSV
                        csv.append(String.format("%s,%d,%d,%d,%.2f,%.2f,%.2f\n",
                                        currentDate.format(dateFormatter),
                                        dailyReferrals,
                                        activeReferrers,
                                        (int) (dailyReferrals * 0.35), // Assume 35% conversion rate
                                        dailyRevenue,
                                        dailyPayouts,
                                        dailyProfit));

                        currentDate = currentDate.plusDays(1);
                }

                return csv.toString().getBytes();
        }
}