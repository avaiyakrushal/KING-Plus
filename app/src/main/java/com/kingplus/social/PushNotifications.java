package com.kingplus.social;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.messaging.FirebaseMessaging;

public final class PushNotifications {
    public static final String CHANNEL_ID = "king_plus_messages";
    public static final int REQUEST_NOTIFICATIONS = 71;

    private PushNotifications() {}

    public static void initialize(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "KING Plus updates", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Messages, room invites, follows and account updates");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    public static void requestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
        }
        refreshToken();
    }

    public static void refreshToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(CloudSync::saveFcmToken);
    }

    public static void show(Context context, String title, String body) {
        initialize(context);
        if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        String safeTitle = title == null || title.trim().isEmpty() ? "KING Plus" : title.trim();
        String safeBody = body == null || body.trim().isEmpty() ? "New update" : body.trim();
        if(safeTitle.length() > 90) safeTitle = safeTitle.substring(0,90);
        if(safeBody.length() > 240) safeBody = safeBody.substring(0,240);
        Intent open = new Intent(context, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT |
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);
        PendingIntent tap = PendingIntent.getActivity(context, 0, open, flags);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(safeTitle)
            .setContentText(safeBody)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(tap)
            .setAutoCancel(true)
            .build();
        try {
            NotificationManagerCompat.from(context).notify(
                (int)(System.currentTimeMillis() & 0x7fffffff), notification);
        } catch (SecurityException ignored) {
            // Android permission was denied, never crash chat/voice from a push.
        }
    }
}
