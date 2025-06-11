package com.kollege.referral.controller;

import com.kollege.referral.model.User;
import com.kollege.referral.repository.UserRepository;
import com.kollege.referral.service.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}", maxAge = 3600)
@Tag(name = "User Management", description = "APIs for managing users and their referral relationships")
public class UserController {
        private final UserRepository userRepository;
        private final ReferralService referralService;

        @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID, including their referral information and earnings")
        @ApiResponse(responseCode = "200", description = "User found", content = @Content(schema = @Schema(implementation = User.class)))
        @ApiResponse(responseCode = "404", description = "User not found")
        @GetMapping("/{id}")
        public ResponseEntity<EntityModel<User>> getUser(
                        @Parameter(description = "ID of the user to retrieve") @PathVariable Long id) {
                return userRepository.findById(id)
                                .map(user -> {
                                        EntityModel<User> userModel = EntityModel.of(user);
                                        Link selfLink = linkTo(methodOn(UserController.class).getUser(id))
                                                        .withSelfRel();
                                        Link referralsLink = linkTo(methodOn(UserController.class).getUserReferrals(id))
                                                        .withRel("referrals");
                                        Link earningsLink = linkTo(methodOn(UserController.class).getUserEarnings(id))
                                                        .withRel("earnings");
                                        userModel.add(selfLink, referralsLink, earningsLink);
                                        return ResponseEntity.ok(userModel);
                                })
                                .orElse(ResponseEntity.notFound().build());
        }

        @Operation(summary = "Get user's referrals", description = "Retrieves both direct and indirect referrals for a user")
        @GetMapping("/{id}/referrals")
        public ResponseEntity<Map<String, CollectionModel<EntityModel<User>>>> getUserReferrals(
                        @Parameter(description = "ID of the user to get referrals for") @PathVariable Long id) {
                List<User> directReferrals = userRepository.findDirectReferrals(id);
                List<User> indirectReferrals = userRepository.findIndirectReferrals(id);

                CollectionModel<EntityModel<User>> directReferralsModel = CollectionModel.of(
                                directReferrals.stream()
                                                .map(user -> EntityModel.of(user,
                                                                linkTo(methodOn(UserController.class)
                                                                                .getUser(user.getId())).withSelfRel()))
                                                .collect(Collectors.toList()));

                CollectionModel<EntityModel<User>> indirectReferralsModel = CollectionModel.of(
                                indirectReferrals.stream()
                                                .map(user -> EntityModel.of(user,
                                                                linkTo(methodOn(UserController.class)
                                                                                .getUser(user.getId())).withSelfRel()))
                                                .collect(Collectors.toList()));

                return ResponseEntity.ok(Map.of(
                                "direct", directReferralsModel,
                                "indirect", indirectReferralsModel));
        }

        @Operation(summary = "Get user's earnings", description = "Retrieves the total, direct, and indirect earnings for a user")
        @GetMapping("/{id}/earnings")
        public ResponseEntity<Map<String, Double>> getUserEarnings(
                        @Parameter(description = "ID of the user to get earnings for") @PathVariable Long id) {
                return userRepository.findById(id)
                                .map(user -> ResponseEntity.ok(Map.of(
                                                "total", user.getTotalEarnings(),
                                                "direct", user.getDirectEarnings(),
                                                "indirect", user.getIndirectEarnings())))
                                .orElse(ResponseEntity.notFound().build());
        }

        @Operation(summary = "Create a new user", description = "Creates a new user with optional referral relationship")
        @PostMapping
        public ResponseEntity<?> createUser(
                        @Parameter(description = "User details to create") @RequestBody User user) {
                // Validate required fields
                if (user.getName() == null || user.getName().trim().isEmpty() ||
                                user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                        return ResponseEntity.badRequest().body("Name and email are required fields");
                }

                // Check if email is already in use
                if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                        return ResponseEntity.badRequest().body("Email is already in use");
                }

                try {
                        // If parent user is specified, handle referral
                        if (user.getParentUser() != null) {
                                Long parentUserId = user.getParentUser().getId();
                                User savedUser = referralService.addReferral(parentUserId, user);
                                EntityModel<User> userModel = EntityModel.of(savedUser,
                                                linkTo(methodOn(UserController.class).getUser(savedUser.getId()))
                                                                .withSelfRel(),
                                                linkTo(methodOn(UserController.class)
                                                                .getUserReferrals(savedUser.getId()))
                                                                .withRel("referrals"),
                                                linkTo(methodOn(UserController.class)
                                                                .getUserEarnings(savedUser.getId()))
                                                                .withRel("earnings"));
                                return ResponseEntity.ok(userModel);
                        } else {
                                // Create user without referral
                                user.setReferralLevel(1);
                                User savedUser = userRepository.save(user);
                                EntityModel<User> userModel = EntityModel.of(savedUser,
                                                linkTo(methodOn(UserController.class).getUser(savedUser.getId()))
                                                                .withSelfRel(),
                                                linkTo(methodOn(UserController.class)
                                                                .getUserReferrals(savedUser.getId()))
                                                                .withRel("referrals"),
                                                linkTo(methodOn(UserController.class)
                                                                .getUserEarnings(savedUser.getId()))
                                                                .withRel("earnings"));
                                return ResponseEntity.ok(userModel);
                        }
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body("Invalid referral: " + e.getMessage());
                } catch (IllegalStateException e) {
                        return ResponseEntity.badRequest().body("Referral not allowed: " + e.getMessage());
                } catch (Exception e) {
                        return ResponseEntity.internalServerError().body("Error creating user: " + e.getMessage());
                }
        }

        @Operation(summary = "Update a user", description = "Updates an existing user's information")
        @PutMapping("/{id}")
        public ResponseEntity<?> updateUser(
                        @Parameter(description = "ID of the user to update") @PathVariable Long id,
                        @Parameter(description = "Updated user details") @RequestBody User user) {
                return userRepository.findById(id)
                                .map(existingUser -> {
                                        try {
                                                // Preserve existing relationships and sensitive data
                                                user.setId(id);
                                                user.setParentUser(existingUser.getParentUser());
                                                user.setReferralLevel(existingUser.getReferralLevel());
                                                user.setReferralCode(existingUser.getReferralCode());
                                                user.setDirectReferrals(existingUser.getDirectReferrals());
                                                user.setTotalEarnings(existingUser.getTotalEarnings());
                                                user.setDirectEarnings(existingUser.getDirectEarnings());
                                                user.setIndirectEarnings(existingUser.getIndirectEarnings());

                                                // Update mutable fields
                                                User updatedUser = userRepository.save(user);
                                                EntityModel<User> userModel = EntityModel.of(updatedUser,
                                                                linkTo(methodOn(UserController.class).getUser(id))
                                                                                .withSelfRel(),
                                                                linkTo(methodOn(UserController.class)
                                                                                .getUserReferrals(id))
                                                                                .withRel("referrals"),
                                                                linkTo(methodOn(UserController.class)
                                                                                .getUserEarnings(id))
                                                                                .withRel("earnings"));
                                                return ResponseEntity.ok(userModel);
                                        } catch (Exception e) {
                                                return ResponseEntity.internalServerError()
                                                                .body("Error updating user: " + e.getMessage());
                                        }
                                })
                                .orElse(ResponseEntity.notFound().build());
        }

        @Operation(summary = "Verify referral code", description = "Checks if a referral code is valid and returns the referrer's details")
        @GetMapping("/verify-referral/{code}")
        public ResponseEntity<Map<String, String>> verifyReferralCode(
                        @Parameter(description = "Referral code to verify") @PathVariable String code) {
                return userRepository.findByReferralCode(code)
                                .map(user -> ResponseEntity.ok(Map.of(
                                                "name", user.getName(),
                                                "email", user.getEmail(),
                                                "referralCode", user.getReferralCode())))
                                .orElse(ResponseEntity.notFound().build());
        }
}