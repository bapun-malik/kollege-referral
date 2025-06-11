package com.kollege.referral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ReferralSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReferralSystemApplication.class, args);
    }
} 