package com.kollege.referral.service;

import com.kollege.referral.dto.EarningNotification;
import com.kollege.referral.model.Earning;
import com.kollege.referral.model.Transaction;
import com.kollege.referral.model.User;
import com.kollege.referral.repository.EarningRepository;
import com.kollege.referral.repository.TransactionRepository;
import com.kollege.referral.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ReferralService {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final EarningRepository earningRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final double LEVEL_1_PERCENTAGE = 0.05; // 5%
    private static final double LEVEL_2_PERCENTAGE = 0.01; // 1%
    private static final double MINIMUM_ELIGIBLE_AMOUNT = 1000.0;

    @Transactional
    public User addReferral(Long parentUserId, User newUser) {
        User parentUser = userRepository.findById(parentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Parent user not found"));

        if (!parentUser.canAddMoreReferrals()) {
            throw new IllegalStateException("Parent user has reached maximum referral limit");
        }

        // Set referral relationships
        newUser.setParentUser(parentUser);
        newUser.setReferralLevel(1); // Direct referral
        newUser.setReferralCode(generateReferralCode());

        return userRepository.save(newUser);
    }

    @Transactional
    public void processPurchase(Long userId, double amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Create and save transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transactionRepository.save(transaction);

        // Process earnings if eligible
        if (transaction.isEligibleForReferralEarnings()) {
            processEarnings(transaction);
        }
    }

    @Transactional
    private void processEarnings(Transaction transaction) {
        User purchaser = transaction.getUser();

        // Process direct referral earnings (Level 1)
        Optional.ofNullable(purchaser.getParentUser())
                .ifPresent(parentUser -> {
                    double directEarning = Earning.calculateEarningAmount(transaction, 1);
                    if (directEarning > 0) {
                        createAndNotifyEarning(parentUser, purchaser, transaction, directEarning, 1);
                        updateUserEarnings(parentUser, directEarning, true); // Update direct earnings
                    }

                    // Process indirect referral earnings (Level 2)
                    Optional.ofNullable(parentUser.getParentUser())
                            .ifPresent(grandParent -> {
                                double indirectEarning = Earning.calculateEarningAmount(transaction, 2);
                                if (indirectEarning > 0) {
                                    createAndNotifyEarning(grandParent, purchaser, transaction, indirectEarning, 2);
                                    updateUserEarnings(grandParent, indirectEarning, false); // Update indirect earnings
                                }
                            });
                });
    }

    @Transactional
    private void updateUserEarnings(User user, double amount, boolean isDirect) {
        if (isDirect) {
            user.setDirectEarnings(user.getDirectEarnings() + amount);
        } else {
            user.setIndirectEarnings(user.getIndirectEarnings() + amount);
        }
        user.setTotalEarnings(user.getDirectEarnings() + user.getIndirectEarnings());
        userRepository.save(user);
    }

    private void createAndNotifyEarning(User parentUser, User sourceUser, Transaction transaction, double amount,
            int level) {
        // Create earning record
        Earning earning = new Earning();
        earning.setParentUser(parentUser);
        earning.setSourceUser(sourceUser);
        earning.setTransaction(transaction);
        earning.setEarnedAmount(amount);
        earning.setLevel(level);
        earningRepository.save(earning);

        // Send real-time notification
        EarningNotification notification = new EarningNotification(
                earning.getId(),
                earning.getEarnedAmount(),
                earning.getLevel(),
                earning.getSourceUser().getName(),
                earning.getSourceUser().getAvatarUrl(),
                earning.getTimestamp(),
                transaction.getAmount());

        messagingTemplate.convertAndSend(
                "/topic/earnings/" + parentUser.getId(),
                notification);
    }

    private String generateReferralCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (userRepository.findByReferralCode(code).isPresent());
        return code;
    }
}