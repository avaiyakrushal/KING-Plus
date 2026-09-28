package com.kingplus.social;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
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
        Notification notification = new Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title == null || title.isEmpty() ? "KING Plus" : title)
            .setContentText(body == null ? "New update" : body)
            .setAutoCancel(true)
            .build();
        NotificationManagerCompat.from(context).notify((int)(System.currentTimeMillis() & 0x7fffffff), notification);
    }
}
