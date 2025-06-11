package com.kollege.referral.controller;

import com.kollege.referral.model.Earning;
import com.kollege.referral.model.Transaction;
import com.kollege.referral.model.User;
import com.kollege.referral.repository.EarningRepository;
import com.kollege.referral.repository.TransactionRepository;
import com.kollege.referral.repository.UserRepository;
import com.kollege.referral.service.ReferralService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final UserRepository userRepository;
    private final EarningRepository earningRepository;
    private final TransactionRepository transactionRepository;
    private final ReferralService referralService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("user", new User());

        // Add total users count
        long totalUsers = userRepository.count();
        model.addAttribute("totalUsers", totalUsers);

        // Calculate total earnings from all sources
        double totalDirectEarnings = userRepository.findAll().stream()
                .mapToDouble(User::getDirectEarnings)
                .sum();
        double totalIndirectEarnings = userRepository.findAll().stream()
                .mapToDouble(User::getIndirectEarnings)
                .sum();
        double totalEarnings = totalDirectEarnings + totalIndirectEarnings;

        model.addAttribute("totalEarnings", totalEarnings);
        model.addAttribute("totalDirectEarnings", totalDirectEarnings);
        model.addAttribute("totalIndirectEarnings", totalIndirectEarnings);

        return "home";
    }

    @PostMapping("/users/create")
    public String createUser(@ModelAttribute User user,
            @RequestParam(required = false) String referralCode,
            RedirectAttributes redirectAttributes) {
        try {
            // Check if email is already in use
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Email is already in use");
                return "redirect:/";
            }

            // Handle referral code
            if (referralCode != null && !referralCode.trim().isEmpty()) {
                User referrer = userRepository.findByReferralCode(referralCode.trim())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid referral code"));

                // Use referral service to handle the referral relationship
                User savedUser = referralService.addReferral(referrer.getId(), user);
                redirectAttributes.addFlashAttribute("message",
                        "User created successfully with referral! User ID: " + savedUser.getId());
                return "redirect:/dashboard/" + savedUser.getId();
            } else {
                // Create user without referral
                user.setParentUser(null); // Clear any partial parent user data
                user.setReferralCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                User savedUser = userRepository.save(user);

                redirectAttributes.addFlashAttribute("message",
                        "User created successfully! User ID: " + savedUser.getId());
                return "redirect:/dashboard/" + savedUser.getId();
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid referral: " + e.getMessage());
            return "redirect:/";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "Referral not allowed: " + e.getMessage());
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error creating user: " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/users")
    public String users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "id,desc") String sort,
            Model model) {

        // Parse sort parameter
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // Create pageable with sort
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // Get users with search if provided
        Page<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository
                    .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrReferralCodeContainingIgnoreCase(
                            search.trim(), search.trim(), search.trim(), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        // Calculate total earnings
        double totalDirectEarnings = userRepository.findAll().stream()
                .mapToDouble(User::getDirectEarnings)
                .sum();
        double totalIndirectEarnings = userRepository.findAll().stream()
                .mapToDouble(User::getIndirectEarnings)
                .sum();
        double totalEarnings = totalDirectEarnings + totalIndirectEarnings;

        model.addAttribute("users", users);
        model.addAttribute("totalEarnings", totalEarnings);
        model.addAttribute("totalDirectEarnings", totalDirectEarnings);
        model.addAttribute("totalIndirectEarnings", totalIndirectEarnings);

        return "users";
    }

    @GetMapping("/transactions")
    public String transactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            Model model) {

        Page<Transaction> transactions;
        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByTimestampBetween(
                    startDate, endDate,
                    PageRequest.of(page, size, Sort.by("timestamp").descending()));
        } else {
            transactions = transactionRepository.findAll(
                    PageRequest.of(page, size, Sort.by("timestamp").descending()));
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "transactions";
    }

    @GetMapping("/earnings")
    public String earnings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            Model model) {

        Page<Earning> earnings;
        if (startDate != null && endDate != null) {
            earnings = earningRepository.findByTimestampBetween(
                    startDate, endDate,
                    PageRequest.of(page, size, Sort.by("timestamp").descending()));
        } else {
            earnings = earningRepository.findAll(
                    PageRequest.of(page, size, Sort.by("timestamp").descending()));
        }

        model.addAttribute("earnings", earnings);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "earnings";
    }

    @GetMapping("/dashboard/{userId}")
    public String dashboard(@PathVariable Long userId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        try {
            // Check if user is logged in
            Long sessionUserId = (Long) session.getAttribute("userId");
            if (sessionUserId == null || !sessionUserId.equals(userId)) {
                // Store the user ID in session
                session.setAttribute("userId", userId);
            }

            // Find user
            User user = userRepository.findById(userId)
                    .orElse(null);

            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/";
            }

            // Get earnings data
            List<Earning> earnings;
            try {
                if (startDate != null && endDate != null) {
                    earnings = earningRepository.findByParentUserIdAndDateRange(userId, startDate, endDate);
                } else {
                    earnings = earningRepository.findByParentUserIdOrderByTimestampDesc(userId);
                }
            } catch (Exception e) {
                earnings = Collections.emptyList();
            }

            // Calculate total earnings by level with safe defaults
            double level1Earnings = 0.0;
            double level2Earnings = 0.0;
            try {
                level1Earnings = Optional.ofNullable(earningRepository.sumEarningsByUserIdAndLevel(userId, 1))
                        .orElse(0.0);
                level2Earnings = Optional.ofNullable(earningRepository.sumEarningsByUserIdAndLevel(userId, 2))
                        .orElse(0.0);
            } catch (Exception e) {
                // Keep default values if query fails
            }

            // Add data to model with null checks
            model.addAttribute("user", user);
            model.addAttribute("earnings", earnings != null ? earnings : Collections.emptyList());
            model.addAttribute("level1Total", level1Earnings);
            model.addAttribute("level2Total", level2Earnings);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);

            // Add referral stats with null checks
            model.addAttribute("directReferrals",
                    user.getDirectReferrals() != null ? user.getDirectReferrals().size() : 0);
            model.addAttribute("maxDirectReferrals", 8);
            model.addAttribute("referralCode", user.getReferralCode() != null ? user.getReferralCode() : "");
            model.addAttribute("totalEarnings", level1Earnings + level2Earnings);

            return "dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while loading the dashboard");
            return "redirect:/";
        }
    }

    @PostMapping("/dashboard/{userId}/purchase")
    public String processPurchase(
            @PathVariable Long userId,
            @RequestParam double amount,
            RedirectAttributes redirectAttributes) {
        try {
            // Validate amount
            if (amount <= 0) {
                redirectAttributes.addFlashAttribute("error", "Purchase amount must be positive");
                return "redirect:/dashboard/" + userId;
            }

            // Find user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Process purchase and referral earnings
            referralService.processPurchase(userId, amount);

            // Add success message
            String message = amount >= 1000
                    ? String.format("Purchase successful! Amount: ₹%.2f. Referral commissions have been processed.",
                            amount)
                    : String.format("Purchase successful! Amount: ₹%.2f. (Minimum ₹1000 for referral earnings)",
                            amount);

            redirectAttributes.addFlashAttribute("message", message);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error processing purchase: " + e.getMessage());
        }

        return "redirect:/dashboard/" + userId;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "You have been logged out successfully");
        return "redirect:/";
    }
}