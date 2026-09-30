package com.kingplus.social;

import com.google.firebase.functions.FirebaseFunctions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CloudBackend {
    public interface Callback { void onResult(boolean ok, String message); }
    private CloudBackend() {}

    private static void call(String name, Map<String,Object> data, Callback callback) {
        FirebaseFunctions.getInstance().getHttpsCallable(name).call(data)
            .addOnSuccessListener(result -> callback.onResult(true, successMessage(result.getData())))
            .addOnFailureListener(error -> callback.onResult(false, "Backend error: " + safe(error.getLocalizedMessage())));
    }

    public static void sendGift(String targetUid, String giftName, int cost, Callback callback) {
        sendGift(targetUid, giftName, cost, "", callback);
    }

    public static void sendGift(String targetUid, String giftName, int cost, String roomId, Callback callback) {
        Map<String,Object> data = new HashMap<>();
        data.put("targetUid", targetUid);
        data.put("giftName", giftName);
        data.put("cost", cost);
        data.put("roomId", roomId == null ? "" : roomId);
        data.put("requestId", UUID.randomUUID().toString());
        call("sendGift", data, callback);
    }

    public static void sendRoomInvite(String targetUid, String roomId, String roomName, Callback callback) {
        Map<String,Object> data = new HashMap<>();
        data.put("targetUid", targetUid); data.put("roomId", roomId); data.put("roomName", roomName);
        call("sendRoomInvite", data, callback);
    }

    public static void sendDirectMessageNotification(String targetUid, String senderName, String preview, Callback callback) {
        Map<String,Object> data = new HashMap<>();
        data.put("targetUid", targetUid);
        data.put("senderName", senderName);
        data.put("preview", preview);
        call("sendDirectMessageNotification", data, callback);
    }

    public static void sendFollowNotification(String targetUid, String followerName, Callback callback) {
        Map<String,Object> data = new HashMap<>();
        data.put("targetUid", targetUid);
        data.put("followerName", followerName);
        call("sendFollowNotification", data, callback);
    }

    public static void moderateReport(String reportId, String status, Callback callback) {
        Map<String,Object> data = new HashMap<>(); data.put("reportId", reportId); data.put("status", status);
        call("moderateReport", data, callback);
    }

    public static void banUser(String targetUid, String reason, Callback callback) {
        Map<String,Object> data = new HashMap<>(); data.put("targetUid", targetUid); data.put("reason", reason);
        call("banUser", data, callback);
    }

    public static void adjustWallet(String targetUid, long delta, String reason, Callback callback) {
        Map<String,Object> data = new HashMap<>(); data.put("targetUid", targetUid); data.put("delta", delta); data.put("reason", reason);
        call("adminAdjustWallet", data, callback);
    }

    private static String successMessage(Object data) {
        if (data instanceof Map) {
            Object message = ((Map<?,?>) data).get("message");
            if (message != null) return String.valueOf(message);
        }
        return "Done";
    }

    private static String safe(String value) { return value == null || value.trim().isEmpty() ? "unknown error" : value; }
}
