package com.kollege.referral.repository;

import com.kollege.referral.model.Earning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface EarningRepository extends JpaRepository<Earning, Long> {
    List<Earning> findByParentUserIdOrderByTimestampDesc(Long parentUserId);

    @Query("SELECT e FROM Earning e WHERE e.parentUser.id = :parentUserId AND e.timestamp BETWEEN :startDate AND :endDate")
    List<Earning> findByParentUserIdAndDateRange(
            @Param("parentUserId") Long parentUserId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(e.earnedAmount) FROM Earning e WHERE e.parentUser.id = :userId AND e.level = :level")
    Double sumEarningsByUserIdAndLevel(@Param("userId") Long userId, @Param("level") int level);

    Page<Earning> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<Earning> findByParentUserIdAndLevel(Long parentUserId, int level);
}