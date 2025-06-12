package com.kollege.referral.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "earnings")
public class Earning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user_id", nullable = false)
    private User parentUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_user_id", nullable = false)
    private User sourceUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(nullable = false)
    private double earnedAmount;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    public static double calculateEarningAmount(Transaction transaction, int level) {
        if (!transaction.isEligibleForReferralEarnings()) {
            return 0;
        }

        double directEarning = transaction.getAmount() * 0.05; // 5% for direct referrals

        return switch (level) {
            case 1 -> directEarning;
            case 2 -> directEarning * 0.01; // 1% of level 1 earnings
            default -> 0;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Earning earning))
            return false;
        return id != null && id.equals(earning.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}