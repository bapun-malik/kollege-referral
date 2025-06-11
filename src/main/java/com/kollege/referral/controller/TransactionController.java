package com.kollege.referral.controller;

import com.kollege.referral.model.Transaction;
import com.kollege.referral.repository.TransactionRepository;
import com.kollege.referral.service.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for managing purchases and transactions")
public class TransactionController {
    private final TransactionRepository transactionRepository;
    private final ReferralService referralService;

    @Operation(summary = "Process a purchase", description = "Records a new purchase and processes referral earnings if eligible")
    @ApiResponse(responseCode = "200", description = "Purchase processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @PostMapping("/purchase")
    public ResponseEntity<String> processPurchase(
            @Parameter(description = "Purchase details", required = true) @RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        double amount = Double.parseDouble(request.get("amount").toString());

        if (amount <= 0) {
            return ResponseEntity.badRequest().body("Invalid amount");
        }

        try {
            referralService.processPurchase(userId, amount);
            return ResponseEntity.ok("Purchase processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get user transactions", description = "Retrieves all transactions for a user, optionally filtered by date range")
    @GetMapping("/user/{userId}")
    public ResponseEntity<CollectionModel<EntityModel<Transaction>>> getUserTransactions(
            @Parameter(description = "ID of the user") @PathVariable Long userId,
            @Parameter(description = "Start date for filtering") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date for filtering") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<Transaction> transactions;
        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        } else {
            transactions = transactionRepository.findByUserIdOrderByTimestampDesc(userId);
        }

        List<EntityModel<Transaction>> transactionModels = transactions.stream()
                .map(transaction -> EntityModel.of(transaction,
                        linkTo(methodOn(TransactionController.class).getUserTransactions(userId, null, null))
                                .withSelfRel()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                CollectionModel.of(transactionModels,
                        linkTo(methodOn(TransactionController.class).getUserTransactions(userId, null, null))
                                .withSelfRel()));
    }

    @Operation(summary = "Get eligible transactions count", description = "Returns the count of transactions eligible for referral earnings")
    @GetMapping("/user/{userId}/eligible-count")
    public ResponseEntity<Map<String, Long>> getEligibleTransactionsCount(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        long count = transactionRepository.countEligibleTransactions(userId);
        return ResponseEntity.ok(Map.of("eligible_count", count));
    }
}