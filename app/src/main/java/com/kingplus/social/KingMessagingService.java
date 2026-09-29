package com.kingplus.social;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

/** Handles FCM tokens and foreground/data notifications, including direct-message deep links. */
public class KingMessagingService extends FirebaseMessagingService {
    @Override public void onNewToken(String token) {
        super.onNewToken(token);
        CloudSync.saveFcmToken(token);
    }

    @Override public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        String title = "KING Plus";
        String body = "New update";
        Map<String,String> data = message.getData();

        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null) title = message.getNotification().getTitle();
            if (message.getNotification().getBody() != null) body = message.getNotification().getBody();
        }
        if (data != null && !data.isEmpty()) {
            if (data.get("title") != null) title = data.get("title");
            if (data.get("body") != null) body = data.get("body");
        }
        PushNotifications.show(this, title, body, data);
    }
}
