package com.kollege.referral.repository;

import com.kollege.referral.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByTimestampDesc(Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.timestamp BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId AND t.amount >= 1000")
    long countEligibleTransactions(@Param("userId") Long userId);

    Page<Transaction> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}