package com.kollege.referral.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningNotificationDTO {
    private Long userId;
    private String userName;
    private Double earnedAmount;
    private Integer level;
    private LocalDateTime timestamp;
    private String referralName;
}