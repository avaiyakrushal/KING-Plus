package com.kingplus.social;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public final class CloudSync {
    public interface Callback { void onResult(boolean ok, String message); }

    private CloudSync() {}

    private static FirebaseUser user() {
        try { return FirebaseAuth.getInstance().getCurrentUser(); }
        catch (Exception ignored) { return null; }
    }

    public static boolean isSignedIn() { return user() != null; }

    public static void syncProfileAndTestWallet(
            Context context,
            SharedPreferences prefs,
            String displayName,
            int coinBalance,
            int giftCount,
            int receivedGiftCount,
            Callback callback) {
        FirebaseUser user = user();
        if (user == null) {
            callback.onResult(false, "Cloud sync needs a Firebase-authenticated Google/phone account. Test mobile login stays local.");
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String,Object> profile = new HashMap<>();
        profile.put("displayName", displayName);
        profile.put("bio", prefs.getString("bio", ""));
        profile.put("hometown", prefs.getString("hometown", ""));
        profile.put("birthday", prefs.getString("birthday", ""));
        profile.put("tags", prefs.getString("tags", ""));
        profile.put("updatedAt", FieldValue.serverTimestamp());

        Map<String,Object> publicProfile = new HashMap<>();
        publicProfile.put("displayName", displayName);
        publicProfile.put("bio", prefs.getString("bio", ""));
        publicProfile.put("tags", prefs.getString("tags", ""));
        publicProfile.put("updatedAt", FieldValue.serverTimestamp());

        Map<String,Object> wallet = new HashMap<>();
        wallet.put("coins", coinBalance);
        wallet.put("giftsSent", giftCount);
        wallet.put("giftsReceived", receivedGiftCount);
        wallet.put("mode", "TEST_CLIENT_SNAPSHOT");
        wallet.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("users").document(user.getUid()).set(profile)
            .continueWithTask(task -> {
                if (!task.isSuccessful()) throw task.getException();
                return db.collection("public_profiles").document(user.getUid()).set(publicProfile);
            })
            .continueWithTask(task -> {
                if (!task.isSuccessful()) throw task.getException();
                return db.collection("test_wallet_snapshots").document(user.getUid()).set(wallet);
            })
            .addOnSuccessListener(unused -> callback.onResult(true, "Profile, public chat identity and TEST wallet snapshot synced to Firestore."))
            .addOnFailureListener(error -> callback.onResult(false, "Cloud sync failed: " + safe(error.getLocalizedMessage())));
    }

    public static void submitReport(Context context, String target, String reason, Callback callback) {
        FirebaseUser user = user();
        if (user == null) {
            callback.onResult(false, "Report saved locally. Sign in with Firebase Google/phone to submit it to the cloud moderation queue.");
            return;
        }
        Map<String,Object> report = new HashMap<>();
        report.put("reporterUid", user.getUid());
        report.put("target", target);
        report.put("reason", reason);
        report.put("status", "new");
        report.put("createdAt", FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance().collection("reports").add(report)
            .addOnSuccessListener(ref -> callback.onResult(true, "Report submitted to the cloud moderation queue."))
            .addOnFailureListener(error -> callback.onResult(false, "Report kept locally; cloud submit failed: " + safe(error.getLocalizedMessage())));
    }

    public static void saveFcmToken(String token) {
        FirebaseUser user = user();
        if (user == null || token == null || token.trim().isEmpty()) return;
        Map<String,Object> data = new HashMap<>();
        data.put("token", token);
        data.put("platform", "android");
        data.put("updatedAt", FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
            .collection("devices").document(token).set(data);
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "unknown error" : value;
    }
}
