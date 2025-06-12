package com.kollege.referral.service.impl;

import com.kollege.referral.dto.EarningResponse;
import com.kollege.referral.dto.EarningsSummaryResponse;
import com.kollege.referral.service.EarningService;
import com.kollege.referral.repository.EarningRepository;
import com.kollege.referral.repository.UserRepository;
import com.kollege.referral.model.Earning;
import com.kollege.referral.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EarningServiceImpl implements EarningService {

        private final EarningRepository earningRepository;
        private final UserRepository userRepository;

        @Override
        public EarningsSummaryResponse getEarningsSummary(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Get current month's earnings
                LocalDateTime currentMonthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                LocalDateTime currentMonthEnd = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay()
                                .minusSeconds(1);
                List<Earning> currentMonthEarnings = earningRepository.findByParentUserIdAndDateRange(
                                userId, currentMonthStart, currentMonthEnd);
                double currentMonthTotal = currentMonthEarnings.stream()
                                .mapToDouble(Earning::getEarnedAmount)
                                .sum();

                // Get previous month's earnings
                LocalDateTime previousMonthStart = currentMonthStart.minusMonths(1);
                LocalDateTime previousMonthEnd = currentMonthStart.minusSeconds(1);
                List<Earning> previousMonthEarnings = earningRepository.findByParentUserIdAndDateRange(
                                userId, previousMonthStart, previousMonthEnd);
                double previousMonthTotal = previousMonthEarnings.stream()
                                .mapToDouble(Earning::getEarnedAmount)
                                .sum();

                // Calculate monthly growth
                double monthlyGrowth = previousMonthTotal > 0
                                ? ((currentMonthTotal - previousMonthTotal) / previousMonthTotal) * 100
                                : 0;

                // Get direct and indirect earnings
                Double directEarnings = earningRepository.sumEarningsByUserIdAndLevel(userId, 1);
                Double indirectEarnings = earningRepository.sumEarningsByUserIdAndLevel(userId, 2);

                // Calculate pending payouts (earnings from last 24 hours)
                LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
                List<Earning> recentEarnings = earningRepository.findByParentUserIdAndDateRange(
                                userId, yesterday, LocalDateTime.now());
                double pendingPayouts = recentEarnings.stream()
                                .mapToDouble(Earning::getEarnedAmount)
                                .sum();

                return EarningsSummaryResponse.builder()
                                .totalEarnings(user.getTotalEarnings())
                                .directReferralEarnings(directEarnings != null ? directEarnings : 0.0)
                                .indirectReferralEarnings(indirectEarnings != null ? indirectEarnings : 0.0)
                                .pendingPayouts(pendingPayouts)
                                .monthlyStats(EarningsSummaryResponse.MonthlyStats.builder()
                                                .currentMonth(currentMonthTotal)
                                                .previousMonth(previousMonthTotal)
                                                .monthlyGrowth(monthlyGrowth)
                                                .build())
                                .build();
        }

        @Override
        public List<EarningResponse> getEarningsHistory(Long userId, LocalDate startDate, LocalDate endDate) {
                LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
                LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

                List<Earning> earnings = earningRepository.findByParentUserIdAndDateRange(userId, start, end);

                return earnings.stream()
                                .map(earning -> EarningResponse.builder()
                                                .id(earning.getId())
                                                .amount(earning.getEarnedAmount())
                                                .referralLevel(earning.getLevel())
                                                .timestamp(earning.getTimestamp())
                                                .referredUser(EarningResponse.ReferredUserInfo.builder()
                                                                .id(earning.getSourceUser().getId())
                                                                .name(earning.getSourceUser().getName())
                                                                .purchaseAmount(earning.getTransaction().getAmount())
                                                                .build())
                                                .build())
                                .collect(Collectors.toList());
        }

        @Override
        public byte[] exportEarnings(Long userId, LocalDate startDate, LocalDate endDate) {
                LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
                LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

                List<Earning> earnings = earningRepository.findByParentUserIdAndDateRange(userId, start, end);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                StringBuilder csv = new StringBuilder();
                csv.append("Date,Amount,Level,Referred User,Purchase Amount\n");

                earnings.forEach(earning -> {
                        csv.append(String.format("%s,%.2f,%d,%s,%.2f\n",
                                        earning.getTimestamp().format(formatter),
                                        earning.getEarnedAmount(),
                                        earning.getLevel(),
                                        earning.getSourceUser().getName(),
                                        earning.getTransaction().getAmount()));
                });

                return csv.toString().getBytes();
        }
}