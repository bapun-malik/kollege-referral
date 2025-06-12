package com.kollege.referral.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Response object containing earning details")
public class EarningResponse {

    @Schema(description = "Unique identifier for the earning", example = "1")
    private Long id;

    @Schema(description = "Amount earned from the referral", example = "100.00")
    private Double amount;

    @Schema(description = "Level of referral (1 for direct, 2 for indirect)", example = "1")
    private Integer referralLevel;

    @Schema(description = "Timestamp when the earning was generated", example = "2024-03-15T10:30:00Z")
    private LocalDateTime timestamp;

    @Schema(description = "Details of the user who generated this earning")
    private ReferredUserInfo referredUser;

    @Data
    @Builder
    public static class ReferredUserInfo {
        @Schema(description = "ID of the referred user", example = "2")
        private Long id;

        @Schema(description = "Name of the referred user", example = "Jane Smith")
        private String name;

        @Schema(description = "Amount of the purchase that generated this earning", example = "1000.00")
        private Double purchaseAmount;
    }
}