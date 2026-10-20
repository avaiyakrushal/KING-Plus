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
        profile.put("uid", user.getUid());
        if (user.getEmail() != null) profile.put("email", user.getEmail());
        if (user.getPhotoUrl() != null) profile.put("photoUrl", user.getPhotoUrl().toString());
        profile.put("authProvider", "firebase");
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

        db.collection("users").document(user.getUid()).set(profile, SetOptions.merge())
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
            callback.onResult(false, "Sign in with your KING Plus account to submit this report.");
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();
        com.google.firebase.firestore.DocumentReference reportRef = db.collection("reports").document();
        com.google.firebase.firestore.DocumentReference historyRef = db.collection("user_report_history")
            .document(uid).collection("items").document(reportRef.getId());
        Map<String,Object> report = new HashMap<>();
        report.put("reporterUid", uid);
        report.put("target", target);
        report.put("reason", reason);
        report.put("status", "new");
        report.put("createdAt", FieldValue.serverTimestamp());
        Map<String,Object> receipt = new HashMap<>();
        receipt.put("reporterUid", uid);
        receipt.put("reportId", reportRef.getId());
        receipt.put("target", target);
        receipt.put("reason", reason);
        receipt.put("submittedAt", FieldValue.serverTimestamp());
        db.batch().set(reportRef, report).set(historyRef, receipt).commit()
            .addOnSuccessListener(unused -> callback.onResult(true,
                "Report submitted. Its account history is synced across your KING Plus devices."))
            .addOnFailureListener(error -> callback.onResult(false,
                "Report was not submitted. Check your connection and try again: " + safe(error.getLocalizedMessage())));
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
