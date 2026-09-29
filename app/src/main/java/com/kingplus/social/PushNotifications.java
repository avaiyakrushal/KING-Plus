package com.kingplus.social;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.Collections;
import java.util.Map;

/** Notification channel, permission, FCM token and chat deep-link helper. */
public final class PushNotifications {
    public static final String CHANNEL_ID = "king_plus_messages";
    public static final int REQUEST_NOTIFICATIONS = 71;

    private PushNotifications() {}

    public static void initialize(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "KING Plus messages", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Private messages, room invites and KING Plus updates");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    public static void requestPermission(Activity activity) {
        initialize(activity);
        if (Build.VERSION.SDK_INT >= 33
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
        }
        refreshToken();
    }

    public static void refreshToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(CloudSync::saveFcmToken);
    }

    public static void show(Context context, String title, String body) {
        show(context, title, body, Collections.emptyMap());
    }

    public static void show(Context context, String title, String body, Map<String,String> data) {
        initialize(context);
        if (Build.VERSION.SDK_INT >= 33
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return;

        Intent intent;
        String type = data == null ? null : data.get("type");
        String senderUid = data == null ? null : data.get("senderUid");
        if ("direct_message".equals(type) && senderUid != null && !senderUid.trim().isEmpty()) {
            intent = new Intent(context, PrivateChatActivity.class);
            intent.putExtra("targetUid", senderUid);
            String senderName = data.get("senderName");
            intent.putExtra("targetName", senderName == null || senderName.trim().isEmpty() ? "KING User" : senderName);
        } else {
            intent = new Intent(context, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int requestCode = (data != null && data.get("threadId") != null)
            ? data.get("threadId").hashCode() : (int)(System.currentTimeMillis() & 0x7fffffff);
        PendingIntent pending = PendingIntent.getActivity(context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) builder = new Notification.Builder(context, CHANNEL_ID);
        else builder = new Notification.Builder(context);

        Notification notification = builder
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle(title == null || title.trim().isEmpty() ? "KING Plus" : title)
            .setContentText(body == null || body.trim().isEmpty() ? "New update" : body)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build();

        NotificationManagerCompat.from(context)
            .notify((int)(System.currentTimeMillis() & 0x7fffffff), notification);
    }
}
