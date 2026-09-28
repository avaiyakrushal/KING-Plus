package com.kingplus.social;

import android.Manifest;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;

public class KingMessagingService extends FirebaseMessagingService {
    public static void register(Context context){
        channel(context);
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();if(user==null)return;
        final String uid=user.getUid();
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token->{FirebaseUser current=FirebaseAuth.getInstance().getCurrentUser();if(current!=null&&uid.equals(current.getUid()))upload(token);});
    }
    private static void upload(String token){Map<String,Object> args=new HashMap<>();args.put("token",token);FirebaseFunctions.getInstance("us-central1").getHttpsCallable("kpRegisterToken").call(args);}
    private static void channel(Context context){if(Build.VERSION.SDK_INT>=26){NotificationManager manager=(NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);manager.createNotificationChannel(new NotificationChannel("king_activity","KING Plus activity",NotificationManager.IMPORTANCE_DEFAULT));}}
    @Override public void onNewToken(String token){if(FirebaseAuth.getInstance().getCurrentUser()!=null)upload(token);}
    @Override public void onMessageReceived(RemoteMessage message){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();Map<String,String> data=message.getData();
        if(user==null||!user.getUid().equals(data.get("uid")))return;
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
        // Verify current account preference to suppress queued messages after opt-out.
        final String uid=user.getUid();
        FirebaseFunctions.getInstance("us-central1").getHttpsCallable("kpBootstrap").call().addOnSuccessListener(result->{
            FirebaseUser current=FirebaseAuth.getInstance().getCurrentUser();if(current==null||!uid.equals(current.getUid()))return;
            if(!(result.getData() instanceof Map)||!Boolean.TRUE.equals(((Map<?,?>)result.getData()).get("notificationsEnabled")))return;
            channel(this);Intent intent=new Intent(this,OnlineActivity.class).putExtra("section","notifications");
            PendingIntent tap=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            Notification.Builder builder=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"king_activity"):new Notification.Builder(this);
            builder.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("KING Plus").setContentText("You have a new notification.").setContentIntent(tap).setAutoCancel(true);
            String notice=data.get("noticeId");((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(notice==null?1:notice.hashCode(),builder.build());
        });
    }
}
