package com.kingplus.social;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.net.URL;

/** Test RTC entry that requires an online Firebase Party membership check. */
final class NativeMeetBridge {
    private NativeMeetBridge() {}

    static void launch(Activity activity, String roomId, String roomName, String displayName, boolean video) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        FirebaseUser user;
        FirebaseFirestore db;
        try {
            user = FirebaseAuth.getInstance().getCurrentUser();
            db = FirebaseFirestore.getInstance();
        } catch (RuntimeException e) {
            toast(activity, "Firebase login could not be verified"); return;
        }
        if (user == null || roomId == null || roomId.trim().isEmpty()) {
            toast(activity, "Sign in and join a live Party first"); return;
        }
        final String uid = user.getUid();
        db.collection("live_rooms").document(roomId).get(Source.SERVER)
            .addOnSuccessListener(room -> {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                if (!room.exists() || Boolean.TRUE.equals(room.getBoolean("closed"))) {
                    toast(activity, "This Party has closed"); return;
                }
                db.collection("live_rooms").document(roomId).collection("members").document(uid).get(Source.SERVER)
                    .addOnSuccessListener(member -> {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        if (!member.exists() || !uid.equals(member.getString("uid"))) {
                            toast(activity, "Join this Party before opening voice"); return;
                        }
                        start(activity, roomId, roomName, displayName, video);
                    })
                    .addOnFailureListener(error -> toast(activity, "Party membership could not be verified online"));
            })
            .addOnFailureListener(error -> toast(activity, "Party could not be verified online"));
    }

    private static void start(Activity activity, String roomId, String roomName, String displayName, boolean video) {
        try {
            String slug = ("KINGPlus-" + roomId).replaceAll("[^A-Za-z0-9_-]", "");
            JitsiMeetUserInfo info = new JitsiMeetUserInfo();
            info.setDisplayName(displayName == null || displayName.trim().isEmpty() ? "KING User" : displayName.trim());
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(new URL("https://meet.jit.si"))
                .setRoom(slug)
                .setSubject(roomName == null || roomName.trim().isEmpty() ? "KING Plus Party" : roomName)
                .setAudioMuted(true)
                .setVideoMuted(!video)
                .setUserInfo(info)
                .setFeatureFlag("welcomepage.enabled", false)
                .setFeatureFlag("prejoinpage.enabled", true)
                .setFeatureFlag("pip.enabled", true)
                .build();
            JitsiMeetActivity.launch(activity, options);
        } catch (Throwable error) {
            Toast.makeText(activity, "Native voice could not start; opening test compatibility room", Toast.LENGTH_LONG).show();
            Intent fallback = new Intent(activity, VoiceWebActivity.class);
            fallback.putExtra("roomId", roomId);
            fallback.putExtra("roomName", roomName);
            fallback.putExtra("displayName", displayName);
            fallback.putExtra("video", video);
            activity.startActivity(fallback);
        }
    }

    private static void toast(Activity activity, String message) {
        if (!activity.isFinishing() && !activity.isDestroyed())
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }
}
