package com.kollege.referral.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @Column(nullable = false, unique = true)
    private String referralCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user_id")
    private User parentUser;

    @OneToMany(mappedBy = "parentUser", cascade = CascadeType.ALL)
    private Set<User> directReferrals = new HashSet<>();

    @Column(nullable = false)
    private int referralLevel = 1;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastActive;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "total_earnings", nullable = false)
    private double totalEarnings = 0.0;

    @Column(name = "direct_earnings", nullable = false)
    private double directEarnings = 0.0;

    @Column(name = "indirect_earnings", nullable = false)
    private double indirectEarnings = 0.0;

    @Version
    private Long version;

    public User() {
        this.lastActive = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastActive == null) {
            lastActive = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastActive = LocalDateTime.now();
    }

    public boolean canAddMoreReferrals() {
        return directReferrals.size() < 8;
    }

    public String getAvatarUrl() {
        // Generate avatar URL using Gravatar or similar service
        return "https://www.gravatar.com/avatar/" + email.hashCode() + "?d=identicon";
    }

    public void addEarning(double amount, boolean isDirect) {
        this.totalEarnings += amount;
        if (isDirect) {
            this.directEarnings += amount;
        } else {
            this.indirectEarnings += amount;
        }
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