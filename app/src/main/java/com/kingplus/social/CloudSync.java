package com.kingplus.social;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
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

    public static void syncPublicProfile(String displayName, String bio, String hometown, String birthday, String tags, Callback callback) {
        FirebaseUser user = user();
        if (user == null) { callback.onResult(false, "Sign in with Firebase to sync your profile."); return; }
        Map<String,Object> profile = new HashMap<>();
        profile.put("uid", user.getUid());
        profile.put("displayName", displayName == null ? "" : displayName.trim());
        profile.put("searchName", displayName == null ? "" : displayName.trim().toLowerCase(java.util.Locale.US));
        profile.put("bio", bio == null ? "" : bio.trim());
        profile.put("hometown", hometown == null ? "" : hometown.trim());
        profile.put("birthday", birthday == null ? "" : birthday.trim());
        profile.put("tags", tags == null ? "" : tags.trim());
        profile.put("updatedAt", FieldValue.serverTimestamp());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        batch.set(db.collection("users").document(user.getUid()), profile, SetOptions.merge());
        batch.set(db.collection("public_profiles").document(user.getUid()), profile, SetOptions.merge());
        batch.commit()
            .addOnSuccessListener(unused -> callback.onResult(true, "Profile synced."))
            .addOnFailureListener(error -> callback.onResult(false, "Profile sync failed: " + safe(error.getLocalizedMessage())));
    }

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

        Map<String,Object> wallet = new HashMap<>();
        wallet.put("coins", coinBalance);
        wallet.put("giftsSent", giftCount);
        wallet.put("giftsReceived", receivedGiftCount);
        wallet.put("mode", "TEST_CLIENT_SNAPSHOT");
        wallet.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("users").document(user.getUid()).set(profile)
            .continueWithTask(task -> {
                if (!task.isSuccessful()) throw task.getException();
                return db.collection("test_wallet_snapshots").document(user.getUid()).set(wallet);
            })
            .addOnSuccessListener(unused -> callback.onResult(true, "Profile and TEST wallet snapshot synced to Firestore."))
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
