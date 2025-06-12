package com.kollege.referral.controller;

import com.kollege.referral.dto.EarningNotificationDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class EarningNotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyEarning(EarningNotificationDTO notification) {
        messagingTemplate.convertAndSend(
                "/topic/earnings/" + notification.getUserId(),
                notification);
    }
}