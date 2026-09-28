package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class LiveCloudActivity extends Activity {
    private final int BG = 0xff101426;
    private final int CARD = 0xff202744;
    private final int PURPLE = 0xff7146ec;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private LinearLayout page;
    private ListenerRegistration roomsListener;
    private ListenerRegistration messagesListener;
    private String openRoomId;
    private String openRoomName;
    private String openRoomOwnerUid;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            new AlertDialog.Builder(this)
                .setTitle("Firebase sign-in required")
                .setMessage("Live rooms, cloud chat, server wallet and push invites require a real Firebase Google/phone account.")
                .setCancelable(false)
                .setPositiveButton("Close", (d,w) -> finish()).show();
            return;
        }
        renderRooms();
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }

    private void base(String title, String subtitle) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(28), dp(20), dp(36));
        scroll.addView(page);
        setContentView(scroll);
        label(title, 27, Color.WHITE, true);
        if (subtitle != null) label(subtitle, 14, 0xffc4bed8, false);
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(null, android.graphics.Typeface.BOLD);
        t.setPadding(dp(4), dp(8), dp(4), dp(8));
        page.addView(t, new LinearLayout.LayoutParams(-1, -2));
        return t;
    }

    private Button button(String text, int color, View.OnClickListener listener) {
        Button b = new Button(this); b.setText(text); b.setAllCaps(false); b.setTextColor(Color.WHITE);
        b.setBackgroundColor(color); b.setOnClickListener(listener);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(54)); p.setMargins(0, dp(5), 0, dp(5));
        page.addView(b, p); return b;
    }

    private EditText input(String hint) {
        EditText e = new EditText(this); e.setHint(hint); e.setTextColor(Color.WHITE); e.setHintTextColor(0xffaca5c3);
        e.setSingleLine(true); e.setBackgroundColor(CARD); e.setPadding(dp(14),0,dp(14),0);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(54)); p.setMargins(0,dp(7),0,dp(7)); page.addView(e,p); return e;
    }

    private void clearListeners() {
        if (roomsListener != null) { roomsListener.remove(); roomsListener = null; }
        if (messagesListener != null) { messagesListener.remove(); messagesListener = null; }
    }

    private void renderRooms() {
        clearListeners(); openRoomId = null;
        base("KING Plus Live", "Firestore real-time rooms + chat • Firebase account connected");
        label("Signed in: " + safeName(), 14, 0xffc4bed8, false);
        EditText roomName = input("New live room name");
        button("＋ Create live room", PURPLE, v -> {
            String name = roomName.getText().toString().trim();
            if (name.length() < 2) { roomName.setError("Enter room name"); return; }
            Map<String,Object> room = new HashMap<>();
            room.put("name", name);
            room.put("ownerUid", user.getUid());
            room.put("ownerName", safeName());
            room.put("createdAt", FieldValue.serverTimestamp());
            room.put("updatedAt", FieldValue.serverTimestamp());
            db.collection("live_rooms").add(room)
                .addOnSuccessListener(ref -> openRoom(ref.getId(), name, user.getUid()))
                .addOnFailureListener(e -> toast("Create failed: " + msg(e)));
        });
        button("💰 Refresh server wallet", CARD, v -> showServerWallet());
        button("🛡 Admin dashboard", CARD, v -> startActivity(new Intent(this, AdminActivity.class)));
        label("Live rooms", 20, Color.WHITE, true);
        LinearLayout list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); page.addView(list, new LinearLayout.LayoutParams(-1,-2));
        roomsListener = db.collection("live_rooms").orderBy("createdAt", Query.Direction.DESCENDING).limit(30)
            .addSnapshotListener((snap,error) -> {
                if (error != null) { toast("Live rooms unavailable: " + msg(error)); return; }
                list.removeAllViews();
                if (snap == null || snap.isEmpty()) {
                    TextView empty = new TextView(this); empty.setText("No cloud rooms yet. Create the first one."); empty.setTextColor(0xffc4bed8); empty.setPadding(dp(8),dp(14),dp(8),dp(14)); list.addView(empty); return;
                }
                for (DocumentSnapshot doc : snap.getDocuments()) {
                    String name = doc.getString("name");
                    String ownerUid = doc.getString("ownerUid");
                    String ownerName = doc.getString("ownerName");
                    Button b = new Button(this); b.setAllCaps(false); b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL); b.setText((name == null ? "Live room" : name) + "\nHost: " + (ownerName == null ? "KING user" : ownerName)); b.setTextColor(Color.WHITE); b.setBackgroundColor(CARD);
                    b.setOnClickListener(v -> openRoom(doc.getId(), name == null ? "Live room" : name, ownerUid));
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(68)); p.setMargins(0,dp(4),0,dp(4)); list.addView(b,p);
                }
            });
    }

    private void openRoom(String roomId, String roomName, String ownerUid) {
        clearListeners();
        openRoomId = roomId; openRoomName = roomName; openRoomOwnerUid = ownerUid;
        base(roomName, "Live cloud room • messages sync across signed-in devices");
        button("‹ Back to live rooms", CARD, v -> renderRooms());
        button("🎤 Join REAL voice test", PURPLE, v -> {
            Intent i = new Intent(this, VoiceWebActivity.class);
            i.putExtra("roomId", roomId); i.putExtra("roomName", roomName); startActivity(i);
        });
        if (ownerUid != null && !ownerUid.equals(user.getUid())) {
            button("🎁 Send 10-coin Rose via server", CARD, v -> CloudBackend.sendGift(ownerUid,"Rose",10,(ok,m)->runOnUiThread(() -> toast(m))));
        }
        EditText inviteUid = input("Invite Firebase UID");
        button("🔔 Send room invite push", CARD, v -> {
            String target = inviteUid.getText().toString().trim();
            if (target.isEmpty()) { inviteUid.setError("Enter target UID"); return; }
            CloudBackend.sendRoomInvite(target, roomId, roomName, (ok,m)->runOnUiThread(() -> toast(m)));
        });
        label("Room chat", 19, Color.WHITE, true);
        LinearLayout chat = new LinearLayout(this); chat.setOrientation(LinearLayout.VERTICAL); page.addView(chat,new LinearLayout.LayoutParams(-1,-2));
        messagesListener = db.collection("live_rooms").document(roomId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING).limit(80)
            .addSnapshotListener((snap,error) -> {
                if (error != null) { toast("Chat unavailable: " + msg(error)); return; }
                chat.removeAllViews();
                if (snap == null) return;
                for (DocumentSnapshot doc : snap.getDocuments()) {
                    String sender = doc.getString("senderName");
                    String text = doc.getString("text");
                    TextView row = new TextView(this); row.setText((sender == null ? "User" : sender) + ": " + (text == null ? "" : text)); row.setTextColor(Color.WHITE); row.setTextSize(15); row.setPadding(dp(10),dp(8),dp(10),dp(8)); chat.addView(row,new LinearLayout.LayoutParams(-1,-2));
                }
            });
        EditText message = input("Message everyone in this room");
        button("Send cloud message", PURPLE, v -> {
            String text = message.getText().toString().trim();
            if (text.isEmpty()) return;
            if (text.length() > 500) { message.setError("Maximum 500 characters"); return; }
            Map<String,Object> data = new HashMap<>();
            data.put("senderUid", user.getUid()); data.put("senderName", safeName()); data.put("text", text); data.put("createdAt", FieldValue.serverTimestamp());
            db.collection("live_rooms").document(roomId).collection("messages").add(data)
                .addOnSuccessListener(r -> message.setText(""))
                .addOnFailureListener(e -> toast("Message failed: " + msg(e)));
        });
    }

    private void showServerWallet() {
        db.collection("wallets").document(user.getUid()).get()
            .addOnSuccessListener(doc -> {
                Number coins = doc.getDouble("coins");
                long balance = coins == null ? 0 : Math.round(coins.doubleValue());
                new AlertDialog.Builder(this).setTitle("Server wallet").setMessage("Authoritative balance: " + balance + " coins\n\nOnly trusted Cloud Functions can change this balance.").setPositiveButton("OK",null).show();
            })
            .addOnFailureListener(e -> toast("Wallet read failed: " + msg(e)));
    }

    private String safeName() {
        String n = user.getDisplayName();
        if (n != null && !n.trim().isEmpty()) return n.trim();
        String phone = user.getPhoneNumber();
        if (phone != null && phone.length() >= 4) return "KING " + phone.substring(phone.length()-4);
        return "KING User";
    }

    private void toast(String s) { Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }
    private String msg(Exception e) { return e == null || e.getLocalizedMessage() == null ? "unknown error" : e.getLocalizedMessage(); }

    @Override public void onBackPressed() {
        if (openRoomId != null) renderRooms(); else super.onBackPressed();
    }

    @Override protected void onDestroy() { clearListeners(); super.onDestroy(); }
}
