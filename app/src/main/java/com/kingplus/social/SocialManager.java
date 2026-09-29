package com.kingplus.social;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Firestore social graph and real-time direct-message foundation for KING Plus. */
public final class SocialManager {
    public interface Result { void done(boolean ok, String message); }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void follow(String me, String target, Result cb) {
        if (invalidPair(me, target)) { cb.done(false, "Invalid user"); return; }
        Map<String,Object> value = new HashMap<>();
        value.put("createdAt", FieldValue.serverTimestamp());
        db.collection("users").document(me).collection("following").document(target).set(value)
            .addOnSuccessListener(x -> db.collection("users").document(target).collection("followers").document(me).set(value)
                .addOnSuccessListener(y -> cb.done(true, "Following"))
                .addOnFailureListener(e -> cb.done(false, safe(e.getMessage()))))
            .addOnFailureListener(e -> cb.done(false, safe(e.getMessage())));
    }

    public void unfollow(String me, String target, Result cb) {
        if (invalidPair(me, target)) { cb.done(false, "Invalid user"); return; }
        db.collection("users").document(me).collection("following").document(target).delete()
            .addOnSuccessListener(x -> db.collection("users").document(target).collection("followers").document(me).delete()
                .addOnSuccessListener(y -> cb.done(true, "Unfollowed"))
                .addOnFailureListener(e -> cb.done(false, safe(e.getMessage()))))
            .addOnFailureListener(e -> cb.done(false, safe(e.getMessage())));
    }

    public void addFriend(String me, String target, Result cb) {
        if (invalidPair(me, target)) { cb.done(false, "Invalid user"); return; }
        Map<String,Object> value = new HashMap<>();
        value.put("createdAt", FieldValue.serverTimestamp());
        db.collection("users").document(me).collection("friends").document(target).set(value)
            .addOnSuccessListener(x -> db.collection("users").document(target).collection("friends").document(me).set(value)
                .addOnSuccessListener(y -> cb.done(true, "Friend added"))
                .addOnFailureListener(e -> cb.done(false, safe(e.getMessage()))))
            .addOnFailureListener(e -> cb.done(false, safe(e.getMessage())));
    }

    public void sendMessage(String me, String target, String senderName, String text, Result cb) {
        sendMessage(me, target, senderName, "KING User", text, cb);
    }

    public void sendMessage(String me, String target, String senderName, String targetName, String text, Result cb) {
        if (invalidPair(me, target)) { cb.done(false, "Invalid chat user"); return; }
        String clean = text == null ? "" : text.trim();
        if (clean.isEmpty()) { cb.done(false, "Message is empty"); return; }
        if (clean.length() > 1000) { cb.done(false, "Maximum 1000 characters"); return; }

        String threadId = threadId(me, target);
        DocumentReference thread = db.collection("direct_threads").document(threadId);

        Map<String,Object> names = new HashMap<>();
        names.put(me, display(senderName));
        names.put(target, display(targetName));

        Map<String,Object> base = new HashMap<>();
        base.put("members", Arrays.asList(me, target));
        base.put("memberNames", names);
        base.put("updatedAt", FieldValue.serverTimestamp());

        thread.set(base, SetOptions.merge())
            .addOnSuccessListener(ignored -> {
                Map<String,Object> message = new HashMap<>();
                message.put("senderUid", me);
                message.put("recipientUid", target);
                message.put("senderName", display(senderName));
                message.put("text", clean);
                message.put("createdAt", FieldValue.serverTimestamp());

                thread.collection("messages").add(message)
                    .addOnSuccessListener(ref -> thread.update(
                            FieldPath.of("lastText"), clean,
                            FieldPath.of("lastSenderUid"), me,
                            FieldPath.of("lastMessageAt"), FieldValue.serverTimestamp(),
                            FieldPath.of("updatedAt"), FieldValue.serverTimestamp(),
                            FieldPath.of("unread", target), FieldValue.increment(1),
                            FieldPath.of("unread", me), 0L)
                        .addOnSuccessListener(x -> cb.done(true, "Message sent"))
                        .addOnFailureListener(e -> cb.done(true, "Message sent")))
                    .addOnFailureListener(e -> cb.done(false, safe(e.getMessage())));
            })
            .addOnFailureListener(e -> cb.done(false, safe(e.getMessage())));
    }

    public ListenerRegistration listenMessages(String me, String target,
            com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot> listener) {
        return db.collection("direct_threads").document(threadId(me, target)).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING).limit(200).addSnapshotListener(listener);
    }

    public ListenerRegistration listenThreads(String me,
            com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot> listener) {
        return db.collection("direct_threads").whereArrayContains("members", me).limit(100).addSnapshotListener(listener);
    }

    public void markThreadRead(String me, String target) {
        if (invalidPair(me, target)) return;
        db.collection("direct_threads").document(threadId(me, target)).update(
            FieldPath.of("unread", me), 0L,
            FieldPath.of("readAt", me), FieldValue.serverTimestamp());
    }

    public static String threadId(String a, String b) {
        return a.compareTo(b) < 0 ? a + "__" + b : b + "__" + a;
    }

    private static boolean invalidPair(String a, String b) {
        return a == null || b == null || a.trim().isEmpty() || b.trim().isEmpty() || a.equals(b);
    }

    private static String display(String value) {
        return value == null || value.trim().isEmpty() ? "KING User" : value.trim();
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "Chat operation failed" : value;
    }
}
