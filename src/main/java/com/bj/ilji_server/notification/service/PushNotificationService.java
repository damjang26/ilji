package com.bj.ilji_server.notification.service;

import com.bj.ilji_server.notification.entity.Notification;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final UserRepository userRepository;

    public void sendPushNotification(Notification notification) {
        User user = userRepository.findById(notification.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
            log.warn("FCM token is missing for user: {}", user.getId());
            return;
        }

        Message message = Message.builder()
                .setToken(user.getFcmToken())
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(notification.getMessageTitle())
                        .setBody(notification.getMessageBody())
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent message: " + response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message", e);
        }
    }
}
