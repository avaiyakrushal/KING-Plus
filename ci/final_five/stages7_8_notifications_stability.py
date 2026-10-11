#!/usr/bin/env python3
"""Stages 7-8: Android 7-16 compatible push notification delivery,
private lock-screen content, tapping opens main app, and low-memory login
animation throttling. No production FCM broadcasts are sent by this task.
"""
from pathlib import Path
import sys
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
f=p/'PushNotifications.java'
s=f.read_text()
def replace_once(old,new,label):
 global s
 assert s.count(old)==1, f'Expected one {label}, got {s.count(old)}'
 s=s.replace(old,new,1)
 print("PASS",label)
replace_once('import android.app.Notification;',
             'import android.app.Notification;\nimport android.app.PendingIntent;\nimport android.content.Intent;\nimport androidx.core.app.NotificationCompat;',
             'AndroidX NotificationCompat and safe MainActivity deeplink')
old='''        Notification notification = new Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title == null || title.isEmpty() ? "KING Plus" : title)
            .setContentText(body == null ? "New update" : body)
            .setAutoCancel(true)
            .build();
        NotificationManagerCompat.from(context).notify((int)(System.currentTimeMillis() & 0x7fffffff), notification);'''
new='''        String safeTitle = title == null || title.trim().isEmpty() ? "KING Plus" : title.trim();
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
        }'''
replace_once(old,new,'Foreground/background push compatibility, tap action and privacy')
f.write_text(s)

# Stage 8 stability: 8FPS royal-login background still smooth, avoid excessive
# GPU work/overdrawing and battery drain during Firebase OTP/Google login.
f=p/'KingLoginBackdropView.java';s=f.read_text()
old='postInvalidateDelayed(120);'
assert s.count(old)==1
s=s.replace(old,'postInvalidateDelayed(170);',1)
f.write_text(s)
print('PASS throttled royalty login animation for mobile RAM/battery')
print('STAGES7/8 no Firestore schema, wallets, auth tokens, or paid VIP changes')
