package com.kollege.referral.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "referral_code", nullable = false, unique = true)
    private String referralCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user_id")
    private User parentUser;

    @OneToMany(mappedBy = "parentUser")
    private Set<User> directReferrals = new HashSet<>();

    @Column(nullable = false)
    private double totalEarnings = 0.0;

    @Column(nullable = false)
    private double directEarnings = 0.0;

    @Column(nullable = false)
    private double indirectEarnings = 0.0;

    @Column(nullable = false)
    private int referralLevel = 1;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastActive;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActive = LocalDateTime.now();
        active = true;
    }

    @PreUpdate
    protected void onUpdate() {
        lastActive = LocalDateTime.now();
    }

    public Set<User> getIndirectReferrals() {
        return directReferrals.stream()
                .flatMap(directRef -> directRef.getDirectReferrals().stream())
                .collect(Collectors.toSet());
    }

    public String getAvatarUrl() {
        try {
            // Convert email to MD5 hash for Gravatar
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(email.toLowerCase().trim().getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return "https://www.gravatar.com/avatar/" + hashtext + "?d=identicon&s=200";
        } catch (NoSuchAlgorithmException e) {
            // Fallback to default identicon if MD5 hashing fails
            return "https://www.gravatar.com/avatar/default?d=identicon&s=200";
        }
    }

    public boolean canAddMoreReferrals() {
        return directReferrals.size() < 8; // Maximum of 8 direct referrals allowed
    }

    public int getReferralLevel() {
        return referralLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof User user))
            return false;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}