package com.kollege.referral.service.impl;

import com.kollege.referral.dto.UserRegistrationRequest;
import com.kollege.referral.dto.UserResponse;
import com.kollege.referral.dto.ReferralTreeResponse;
import com.kollege.referral.service.UserService;
import com.kollege.referral.repository.UserRepository;
import com.kollege.referral.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;

        @Override
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