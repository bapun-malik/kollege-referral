package com.kollege.referral.service.impl;

import com.kollege.referral.dto.UserRegistrationRequest;
import com.kollege.referral.dto.UserResponse;
import com.kollege.referral.dto.ReferralTreeResponse;
import com.kollege.referral.service.UserService;
import com.kollege.referral.repository.UserRepository;
import com.kollege.referral.repository.EarningRepository;
import com.kollege.referral.model.User;
import com.kollege.referral.model.Earning;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;
        private final EarningRepository earningRepository;

        @Override
        public byte[] exportUsers() {
                StringBuilder csv = new StringBuilder();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                // Headers
                csv.append("User ID,Name,Email,Referral Code,Total Earnings,Direct Earnings,Indirect Earnings,Direct Referrals,Indirect Referrals,Created At,Last Active\n");

                // Data rows
                userRepository.findAll().forEach(user -> {
                        csv.append(String.format("%d,%s,%s,%s,%.2f,%.2f,%.2f,%d,%d,%s,%s\n",
                                        user.getId(),
                                        escapeCsvField(user.getName()),
                                        escapeCsvField(user.getEmail()),
                                        user.getReferralCode(),
                                        user.getTotalEarnings(),
                                        user.getDirectEarnings(),
                                        user.getIndirectEarnings(),
                                        user.getDirectReferrals().size(),
                                        user.getIndirectReferrals().size(),
                                        user.getCreatedAt().format(dateFormatter),
                                        user.getLastActive().format(dateFormatter)));
                });

                return csv.toString().getBytes();
        }

        @Override
        public byte[] exportUserDetails(Long userId, LocalDate startDate, LocalDate endDate) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
                LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                StringBuilder csv = new StringBuilder();

                // User Profile Section
                csv.append("User Profile\n");
                csv.append("ID,Name,Email,Referral Code,Created At,Last Active\n");
                csv.append(String.format("%d,%s,%s,%s,%s,%s\n\n",
                                user.getId(),
                                escapeCsvField(user.getName()),
                                escapeCsvField(user.getEmail()),
                                user.getReferralCode(),
                                user.getCreatedAt().format(dateFormatter),
                                user.getLastActive().format(dateFormatter)));

                // Earnings Summary Section
                csv.append("Earnings Summary\n");
                csv.append("Total Earnings,Direct Earnings,Indirect Earnings\n");
                csv.append(String.format("%.2f,%.2f,%.2f\n\n",
                                user.getTotalEarnings(),
                                user.getDirectEarnings(),
                                user.getIndirectEarnings()));

                // Detailed Earnings Section
                csv.append("Detailed Earnings\n");
                csv.append("Date,Amount,Level,Source User,Transaction Amount\n");

                List<Earning> earnings = earningRepository.findByParentUserIdAndDateRange(userId, start, end);
                earnings.forEach(earning -> {
                        csv.append(String.format("%s,%.2f,%d,%s,%.2f\n",
                                        earning.getTimestamp().format(dateFormatter),
                                        earning.getEarnedAmount(),
                                        earning.getLevel(),
                                        escapeCsvField(earning.getSourceUser().getName()),
                                        earning.getTransaction().getAmount()));
                });

                return csv.toString().getBytes();
        }

        @Override
        public byte[] exportUserReferralNetwork(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                StringBuilder csv = new StringBuilder();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                // Direct Referrals Section
                csv.append("Direct Referrals (Level 1)\n");
                csv.append("ID,Name,Email,Referral Code,Total Earnings,Created At,Last Active\n");

                user.getDirectReferrals().forEach(referral -> {
                        csv.append(String.format("%d,%s,%s,%s,%.2f,%s,%s\n",
                                        referral.getId(),
                                        escapeCsvField(referral.getName()),
                                        escapeCsvField(referral.getEmail()),
                                        referral.getReferralCode(),
                                        referral.getTotalEarnings(),
                                        referral.getCreatedAt().format(dateFormatter),
                                        referral.getLastActive().format(dateFormatter)));
                });

                // Indirect Referrals Section
                csv.append("\nIndirect Referrals (Level 2)\n");
                csv.append("ID,Name,Email,Referral Code,Total Earnings,Created At,Last Active,Parent Referrer\n");

                user.getIndirectReferrals().forEach(referral -> {
                        csv.append(String.format("%d,%s,%s,%s,%.2f,%s,%s,%s\n",
                                        referral.getId(),
                                        escapeCsvField(referral.getName()),
                                        escapeCsvField(referral.getEmail()),
                                        referral.getReferralCode(),
                                        referral.getTotalEarnings(),
                                        referral.getCreatedAt().format(dateFormatter),
                                        referral.getLastActive().format(dateFormatter),
                                        escapeCsvField(referral.getParentUser().getName())));
                });

                // Network Statistics
                csv.append("\nNetwork Statistics\n");
                csv.append("Metric,Value\n");
                csv.append(String.format("Total Network Size,%d\n",
                                user.getDirectReferrals().size() + user.getIndirectReferrals().size()));
                csv.append(String.format("Direct Referrals,%d\n", user.getDirectReferrals().size()));
                csv.append(String.format("Indirect Referrals,%d\n", user.getIndirectReferrals().size()));
                csv.append(String.format("Network Total Earnings,%.2f\n",
                                user.getDirectReferrals().stream().mapToDouble(User::getTotalEarnings).sum() +
                                                user.getIndirectReferrals().stream().mapToDouble(User::getTotalEarnings)
                                                                .sum()));

                return csv.toString().getBytes();
        }

        private String escapeCsvField(String field) {
                if (field == null) {
                        return "";
                }
                return "\"" + field.replace("\"", "\"\"") + "\"";
        }

        @Override
        @Transactional
        public UserResponse registerUser(UserRegistrationRequest request) {
                // Check if email already exists
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                        throw new RuntimeException("Email already registered");
                }

                User user = new User();
                user.setEmail(request.getEmail());
                user.setName(request.getName());

                // Generate unique referral code
                String referralCode;
                do {
                        referralCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                } while (userRepository.findByReferralCode(referralCode).isPresent());
                user.setReferralCode(referralCode);

                // Link to referrer if referral code provided
                if (request.getReferralCode() != null) {
                        User referrer = userRepository.findByReferralCode(request.getReferralCode())
                                        .orElseThrow(() -> new RuntimeException("Invalid referral code"));

                        if (!referrer.canAddMoreReferrals()) {
                                throw new RuntimeException("Referrer has reached maximum referrals limit");
                        }

                        user.setParentUser(referrer);
                        user.setReferralLevel(referrer.getReferralLevel() + 1);
                }

                user = userRepository.save(user);

                return UserResponse.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .name(user.getName())
                                .referralCode(user.getReferralCode())
                                .referralLink("https://kollege.com/ref/" + user.getReferralCode())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public ReferralTreeResponse getReferralTree(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<User> directReferrals = userRepository.findDirectReferrals(userId);
                List<User> indirectReferrals = userRepository.findIndirectReferrals(userId);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

                return ReferralTreeResponse.builder()
                                .userId(userId)
                                .directReferrals(ReferralTreeResponse.ReferralGroup.builder()
                                                .count(directReferrals.size())
                                                .users(directReferrals.stream()
                                                                .map(ref -> ReferralTreeResponse.ReferredUser.builder()
                                                                                .id(ref.getId())
                                                                                .name(ref.getName())
                                                                                .registrationDate(ref.getCreatedAt()
                                                                                                .format(formatter))
                                                                                .totalPurchases(ref.getTotalEarnings())
                                                                                .build())
                                                                .collect(Collectors.toList()))
                                                .build())
                                .indirectReferrals(ReferralTreeResponse.ReferralGroup.builder()
                                                .count(indirectReferrals.size())
                                                .users(indirectReferrals.stream()
                                                                .map(ref -> ReferralTreeResponse.ReferredUser.builder()
                                                                                .id(ref.getId())
                                                                                .name(ref.getName())
                                                                                .registrationDate(ref.getCreatedAt()
                                                                                                .format(formatter))
                                                                                .totalPurchases(ref.getTotalEarnings())
                                                                                .build())
                                                                .collect(Collectors.toList()))
                                                .build())
                                .build();
        }

        @Override
        @Transactional
        public Object generateNewReferralCode(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Generate new unique referral code
                final String newReferralCode = generateUniqueReferralCode();
                user.setReferralCode(newReferralCode);
                userRepository.save(user);

                return new Object() {
                        public final String referralCode = newReferralCode;
                        public final String referralLink = "https://kollege.com/ref/" + newReferralCode;
                };
        }

        private String generateUniqueReferralCode() {
                String referralCode;
                do {
                        referralCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                } while (userRepository.findByReferralCode(referralCode).isPresent());
                return referralCode;
        }
}