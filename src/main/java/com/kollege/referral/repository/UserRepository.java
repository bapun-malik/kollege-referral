package com.kollege.referral.repository;

import com.kollege.referral.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByReferralCode(String referralCode);

    @Query("SELECT u FROM User u WHERE u.parentUser.id = :userId")
    List<User> findDirectReferrals(@Param("userId") Long userId);

    @Query("SELECT u FROM User u WHERE u.parentUser.parentUser.id = :userId")
    List<User> findIndirectReferrals(@Param("userId") Long userId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.parentUser.id = :userId")
    long countDirectReferrals(@Param("userId") Long userId);

    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrReferralCodeContainingIgnoreCase(
            String name, String email, String referralCode, Pageable pageable);
}