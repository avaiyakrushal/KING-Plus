package com.kingplus.social;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class KingMessagingService extends FirebaseMessagingService {
    @Override public void onNewToken(String token) {
        super.onNewToken(token);
        CloudSync.saveFcmToken(token);
    }

    @Override public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        String title = "KING Plus";
        String body = "New update";
        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null) title = message.getNotification().getTitle();
            if (message.getNotification().getBody() != null) body = message.getNotification().getBody();
        } else if (!message.getData().isEmpty()) {
            if (message.getData().get("title") != null) title = message.getData().get("title");
            if (message.getData().get("body") != null) body = message.getData().get("body");
        }
        PushNotifications.show(this, title, body);
    }
}
