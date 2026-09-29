package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InboxActivity extends Activity {
    private static final int BG = 0xfff7f7fb;
    private static final int PURPLE = 0xff7c4dff;
    private static final int DARK = 0xff181528;

    private LinearLayout list;
    private ListenerRegistration threadListener;
    private FirebaseUser me;
    private FirebaseFirestore db;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        try { me = FirebaseAuth.getInstance().getCurrentUser(); } catch (Exception ignored) { me = null; }
        try { db = FirebaseFirestore.getInstance(); } catch (Exception ignored) { db = null; }
        renderShell();
        loadInbox();
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    private void renderShell() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);

        LinearLayout head = new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL); head.setPadding(dp(14), dp(14), dp(10), dp(10));
        TextView back = label("‹", 38, DARK, false); back.setGravity(Gravity.CENTER); back.setOnClickListener(v -> finish());
        head.addView(back, new LinearLayout.LayoutParams(dp(48), dp(54)));
        LinearLayout titleWrap = new LinearLayout(this); titleWrap.setOrientation(LinearLayout.VERTICAL);
        TextView title = label("Messages", 28, DARK, true); titleWrap.addView(title);
        TextView status = label(me == null ? "Local / test mode" : "Realtime chat connected", 12, 0xff777186, false); titleWrap.addView(status);
        head.addView(titleWrap, new LinearLayout.LayoutParams(0, dp(60), 1));
        TextView plus = label("＋", 30, PURPLE, true); plus.setGravity(Gravity.CENTER); plus.setOnClickListener(v -> newChatDialog());
        head.addView(plus, new LinearLayout.LayoutParams(dp(56), dp(56)));
        root.addView(head);

        LinearLayout quick = new LinearLayout(this); quick.setPadding(dp(14), dp(4), dp(14), dp(8)); quick.setGravity(Gravity.CENTER);
        addQuick(quick, "💬\nChats", () -> Toast.makeText(this,"All conversations",Toast.LENGTH_SHORT).show());
        addQuick(quick, "👥\nFriends", () -> openLocal("Friends"));
        addQuick(quick, "🔔\nAlerts", () -> openLocal("KING Plus Official"));
        root.addView(quick, new LinearLayout.LayoutParams(-1, dp(78)));

        ScrollView sv = new ScrollView(this); list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(12), dp(4), dp(12), dp(24)); sv.addView(list);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void addQuick(LinearLayout row, String text, Runnable action) {
        TextView x = label(text, 13, DARK, true); x.setGravity(Gravity.CENTER); x.setBackground(bg(Color.WHITE, 18));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(68), 1); p.setMargins(dp(4), 0, dp(4), 0); row.addView(x, p); x.setOnClickListener(v -> action.run());
    }

    private void loadInbox() {
        list.removeAllViews();
        addThread("🔥", "KING Party Room", "Room chat and invitations", "");
        addThread("👑", "KING Plus Official", "Welcome and account updates", "");

        Set<String> local = getSharedPreferences("chat_store", MODE_PRIVATE).getStringSet("recent_names", new HashSet<>());
        for (String name : local) if (!"KING Party Room".equals(name) && !"KING Plus Official".equals(name)) addThread("●", name, "Tap to continue chatting", "");

        if (me == null || db == null) {
            addThread("🎵", "Lily", "See you in the music room ✨", "");
            addThread("🎮", "Alex", "Let's play tonight", "");
            return;
        }

        threadListener = db.collection("direct_threads").whereArrayContains("members", me.getUid())
            .addSnapshotListener((snap, error) -> {
                if (error != null) {
                    Toast.makeText(this, "Cloud inbox unavailable • local chat still works", Toast.LENGTH_SHORT).show();
                    return;
                }
                renderCloudThreads(snap);
            });
    }

    private void renderCloudThreads(QuerySnapshot snap) {
        if (snap == null) return;
        List<DocumentSnapshot> docs = new ArrayList<>(snap.getDocuments());
        Collections.sort(docs, (a,b) -> {
            Timestamp ta = a.getTimestamp("updatedAt"), tb = b.getTimestamp("updatedAt");
            long aa = ta == null ? 0 : ta.toDate().getTime(); long bb = tb == null ? 0 : tb.toDate().getTime();
            return Long.compare(bb, aa);
        });
        for (DocumentSnapshot doc : docs) {
            List<String> members = (List<String>) doc.get("members");
            if (members == null) continue;
            String peerUid = null; for (String uid : members) if (!me.getUid().equals(uid)) { peerUid = uid; break; }
            if (peerUid == null) continue;
            String peerName = peerUid;
            Object namesObj = doc.get("memberNames");
            if (namesObj instanceof Map) {
                Object n = ((Map<?,?>) namesObj).get(peerUid); if (n != null) peerName = String.valueOf(n);
            }
            String last = doc.getString("lastMessage"); if (last == null || last.trim().isEmpty()) last = "Open conversation";
            boolean unread = isUnread(doc);
            addThread(unread ? "●" : "○", peerName, (unread ? "NEW • " : "") + last, peerUid);
        }
    }

    private boolean isUnread(DocumentSnapshot doc) {
        if (me == null) return false;
        String lastSender = doc.getString("lastSenderUid"); if (me.getUid().equals(lastSender)) return false;
        Timestamp updated = doc.getTimestamp("updatedAt"); if (updated == null) return false;
        Object readObj = doc.get("readAt");
        if (!(readObj instanceof Map)) return true;
        Object own = ((Map<?,?>) readObj).get(me.getUid());
        if (!(own instanceof Timestamp)) return true;
        return ((Timestamp) own).compareTo(updated) < 0;
    }

    private void addThread(String icon, String name, String preview, String peerUid) {
        LinearLayout card = new LinearLayout(this); card.setGravity(Gravity.CENTER_VERTICAL); card.setPadding(dp(14), dp(10), dp(12), dp(10)); card.setBackground(bg(Color.WHITE, 18));
        TextView avatar = label(icon, 25, PURPLE, true); avatar.setGravity(Gravity.CENTER); avatar.setBackground(bg(0xfff0eaff, 25)); card.addView(avatar, new LinearLayout.LayoutParams(dp(50), dp(50)));
        LinearLayout info = new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(12), 0, dp(4), 0);
        TextView n = label(name, 16, DARK, true); info.addView(n); TextView p = label(preview, 13, 0xff777186, false); p.setMaxLines(1); info.addView(p);
        card.addView(info, new LinearLayout.LayoutParams(0, dp(54), 1)); TextView arrow = label("›", 28, 0xff9993a6, false); card.addView(arrow);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(74)); lp.setMargins(0, dp(5), 0, dp(5)); list.addView(card, lp);
        card.setOnClickListener(v -> openChat(name, peerUid));
    }

    private void newChatDialog() {
        final EditText name = new EditText(this); name.setHint("Friend name");
        final EditText uid = new EditText(this); uid.setHint("Firebase UID (optional for realtime)");
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(18),0,dp(18),0); box.addView(name); box.addView(uid);
        new AlertDialog.Builder(this).setTitle("New message").setMessage("Name is enough for local/test chat. Add the Firebase UID for cross-device realtime chat.")
            .setView(box).setNegativeButton("Cancel", null).setPositiveButton("Open", (d,w) -> {
                String n = name.getText().toString().trim(); if (n.isEmpty()) n = "New Friend";
                openChat(n, uid.getText().toString().trim());
            }).show();
    }

    private void openLocal(String name) { openChat(name, ""); }
    private void openChat(String name, String peerUid) {
        Intent i = new Intent(this, ChatActivity.class); i.putExtra("peerName", name); i.putExtra("peerUid", peerUid); startActivity(i);
    }

    @Override protected void onDestroy() {
        if (threadListener != null) threadListener.remove();
        super.onDestroy();
    }
}
