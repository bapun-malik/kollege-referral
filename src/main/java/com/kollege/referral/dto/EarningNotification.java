package com.kollege.referral.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EarningNotification {
    private Long earningId;
    private double earnedAmount;
    private int level;
    private String sourceUserName;
    private String sourceUserAvatar;
    private LocalDateTime timestamp;
    private double transactionAmount;
}