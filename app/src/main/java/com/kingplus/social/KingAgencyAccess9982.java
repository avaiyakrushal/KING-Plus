package com.kingplus.social;

import android.app.Activity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

/** Displays Agency entry points only to server-authorized accounts. */
public final class KingAgencyAccess9982 {
    public interface Callback { void completed(boolean allowed); }
    private KingAgencyAccess9982() {}

    public static void check(Activity activity, Callback callback) {
        if (activity == null || callback == null) return;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { callback.completed(false); return; }
        final String uid = user.getUid();
        user.getIdToken(false).addOnSuccessListener(token -> {
            if (!current(activity, uid)) return;
            if (Boolean.TRUE.equals(token.getClaims().get("admin"))) {
                callback.completed(true); return;
            }
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("king_agencies").document(uid).get(Source.SERVER)
              .addOnSuccessListener(agency -> {
                if (!current(activity, uid)) return;
                if (agency.exists() && Boolean.TRUE.equals(agency.getBoolean("active"))) {
                    callback.completed(true); return;
                }
                db.collection("king_agents").document(uid).get(Source.SERVER)
                  .addOnSuccessListener(agent -> {
                    if (current(activity, uid)) callback.completed(agent.exists() &&
                        Boolean.TRUE.equals(agent.getBoolean("active")));
                  }).addOnFailureListener(error -> {
                    if (current(activity, uid)) callback.completed(false);
                  });
              }).addOnFailureListener(error -> {
                if (current(activity, uid)) callback.completed(false);
              });
        }).addOnFailureListener(error -> {
            if (current(activity, uid)) callback.completed(false);
        });
    }

    private static boolean current(Activity activity, String uid) {
        FirebaseUser now = FirebaseAuth.getInstance().getCurrentUser();
        return !activity.isFinishing() && !activity.isDestroyed() && now != null &&
            uid.equals(now.getUid());
    }
}
