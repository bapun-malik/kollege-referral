package com.kollege.referral.controller;

import com.kollege.referral.model.Earning;
import com.kollege.referral.repository.EarningRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/earnings")
@RequiredArgsConstructor
@Tag(name = "Earnings Management", description = "APIs for managing and retrieving referral earnings. Provides endpoints for querying earnings by user, level, and date ranges.")
public class EarningController {
        private final EarningRepository earningRepository;
        private final SimpMessagingTemplate messagingTemplate;

        @Operation(summary = "Get user earnings", description = "Retrieves all earnings for a user, optionally filtered by date range. Returns both direct (level 1) and indirect (level 2) earnings.")
        @GetMapping("/user/{userId}")
        public ResponseEntity<CollectionModel<EntityModel<Earning>>> getUserEarnings(
                        @Parameter(description = "ID of the user to fetch earnings for", example = "1") @PathVariable Long userId,
                        @Parameter(description = "Start date for filtering (ISO format)", example = "2024-01-01T00:00:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                        @Parameter(description = "End date for filtering (ISO format)", example = "2024-12-31T23:59:59") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

                List<Earning> earnings;
                if (startDate != null && endDate != null) {
                        earnings = earningRepository.findByParentUserIdAndDateRange(userId, startDate, endDate);
                } else {
                        earnings = earningRepository.findByParentUserIdOrderByTimestampDesc(userId);
                }

                List<EntityModel<Earning>> earningModels = earnings.stream()
                                .map(earning -> EntityModel.of(earning,
                                                linkTo(methodOn(EarningController.class).getUserEarnings(userId, null,
                                                                null)).withSelfRel(),
                                                linkTo(methodOn(EarningController.class).getEarningsByLevel(userId))
                                                                .withRel("by-level")))
                                .collect(Collectors.toList());

                return ResponseEntity.ok(
                                CollectionModel.of(earningModels,
                                                linkTo(methodOn(EarningController.class).getUserEarnings(userId, null,
                                                                null)).withSelfRel()));
        }

        @Operation(summary = "Get earnings by level", description = "Retrieves the total earnings grouped by referral level. Level 1 represents direct referrals (5% commission), and Level 2 represents indirect referrals (1% commission).")
        @GetMapping("/user/{userId}/by-level")
        public ResponseEntity<Map<String, Double>> getEarningsByLevel(
                        @Parameter(description = "ID of the user to fetch earnings for", example = "1") @PathVariable Long userId) {
                Double level1Earnings = earningRepository.sumEarningsByUserIdAndLevel(userId, 1);
                Double level2Earnings = earningRepository.sumEarningsByUserIdAndLevel(userId, 2);

                return ResponseEntity.ok(Map.of(
                                "level1", level1Earnings != null ? level1Earnings : 0.0,
                                "level2", level2Earnings != null ? level2Earnings : 0.0));
        }

        @Operation(summary = "Get earnings by specific level", description = "Retrieves all earnings for a specific referral level. Use level 1 for direct referral earnings (5%) or level 2 for indirect referral earnings (1%).")
        @GetMapping("/user/{userId}/level/{level}")
        public ResponseEntity<CollectionModel<EntityModel<Earning>>> getEarningsByUserAndLevel(
                        @Parameter(description = "ID of the user to fetch earnings for", example = "1") @PathVariable Long userId,
                        @Parameter(description = "Referral level (1 for direct 5%, 2 for indirect 1%)", example = "1") @PathVariable int level) {

                List<EntityModel<Earning>> earningModels = earningRepository.findByParentUserIdAndLevel(userId, level)
                                .stream()
                                .map(earning -> EntityModel.of(earning,
                                                linkTo(methodOn(EarningController.class).getUserEarnings(userId, null,
                                                                null)).withSelfRel()))
                                .collect(Collectors.toList());

                return ResponseEntity.ok(
                                CollectionModel.of(earningModels,
                                                linkTo(methodOn(EarningController.class)
                                                                .getEarningsByUserAndLevel(userId, level))
                                                                .withSelfRel(),
                                                linkTo(methodOn(EarningController.class).getEarningsByLevel(userId))
                                                                .withRel("summary")));
        }

        @Operation(summary = "Get earnings trend", description = "Retrieves the earnings trend over time for a specific user. Groups earnings by date and returns daily totals.")
        @GetMapping("/trend/{userId}")
        public Map<String, Double> getEarningsTrend(
                        @Parameter(description = "ID of the user to fetch trend for", example = "1") @PathVariable Long userId,
                        @Parameter(description = "Timeframe for trend analysis (daily, weekly, monthly)", example = "daily") @RequestParam String timeframe,
                        @Parameter(description = "Start date for trend analysis (ISO format)", example = "2024-01-01T00:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                        @Parameter(description = "End date for trend analysis (ISO format)", example = "2024-12-31T23:59:59") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

                List<Earning> earnings = earningRepository.findByParentUserIdAndDateRange(userId, startDate, endDate);

                return earnings.stream()
                                .collect(Collectors.groupingBy(
                                                earning -> earning.getTimestamp().toLocalDate().toString(),
                                                Collectors.collectingAndThen(
                                                                Collectors.toList(),
                                                                list -> list.stream()
                                                                                .mapToDouble(Earning::getEarnedAmount)
                                                                                .sum())));
        }

        @Operation(summary = "Get earnings distribution", description = "Analyzes the distribution of earnings between direct and indirect referrals for a specific time period.")
        @GetMapping("/distribution/{userId}")
        public Map<String, Double> getEarningsDistribution(
                        @Parameter(description = "ID of the user to analyze", example = "1") @PathVariable Long userId,
                        @Parameter(description = "Start date for analysis (ISO format)", example = "2024-01-01T00:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                        @Parameter(description = "End date for analysis (ISO format)", example = "2024-12-31T23:59:59") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

                List<Earning> earnings = earningRepository.findByParentUserIdAndDateRange(userId, startDate, endDate);

                return earnings.stream()
                                .collect(Collectors.groupingBy(
                                                earning -> earning.getLevel() == 1 ? "Direct" : "Indirect",
                                                Collectors.collectingAndThen(
                                                                Collectors.toList(),
                                                                list -> list.stream()
                                                                                .mapToDouble(Earning::getEarnedAmount)
                                                                                .sum())));
        }

        @Operation(summary = "Get earnings per referral", description = "Breaks down earnings by individual referrals, showing how much each referred user has contributed to the total earnings.")
        @GetMapping("/per-referral/{userId}")
        public Map<Long, Double> getEarningsPerReferral(
                        @Parameter(description = "ID of the user to analyze", example = "1") @PathVariable Long userId,
                        @Parameter(description = "Start date for analysis (ISO format)", example = "2024-01-01T00:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                        @Parameter(description = "End date for analysis (ISO format)", example = "2024-12-31T23:59:59") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

                List<Earning> earnings = earningRepository.findByParentUserIdAndDateRange(userId, startDate, endDate);

                return earnings.stream()
                                .collect(Collectors.groupingBy(
                                                earning -> earning.getSourceUser().getId(),
                                                Collectors.collectingAndThen(
                                                                Collectors.toList(),
                                                                list -> list.stream()
                                                                                .mapToDouble(Earning::getEarnedAmount)
                                                                                .sum())));
        }
}