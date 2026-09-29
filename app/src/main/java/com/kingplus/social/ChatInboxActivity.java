package com.kingplus.social;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Inbox of real Firestore direct-message threads for the signed-in user. */
public class ChatInboxActivity extends Activity {
    private final SocialManager social = new SocialManager();
    private String me;
    private LinearLayout list;
    private TextView status;
    private ListenerRegistration listener;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        me = FirebaseAuth.getInstance().getUid();
        if (me == null) { finish(); return; }
        render();
        PushNotifications.requestPermission(this);
        listen();
    }

    private void render() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xfff7f7fb);
        root.setPadding(dp(16), dp(18), dp(16), dp(12));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this);
        title.setText("Messages");
        title.setTextSize(30);
        title.setTextColor(0xff171717);
        header.addView(title, new LinearLayout.LayoutParams(0, dp(60), 1));

        Button find = new Button(this);
        find.setText("＋ New");
        find.setAllCaps(false);
        find.setOnClickListener(v -> startActivity(new Intent(this, UserSocialActivity.class)));
        header.addView(find, new LinearLayout.LayoutParams(dp(100), dp(54)));
        root.addView(header);

        status = new TextView(this);
        status.setText("Loading chats…");
        status.setTextColor(0xff777777);
        status.setPadding(dp(4), dp(8), dp(4), dp(12));
        root.addView(status);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
    }

    private void listen() {
        listener = social.listenThreads(me, (snapshot, error) -> {
            if (error != null) {
                runOnUiThread(() -> status.setText("Chat sync failed: " + safe(error.getLocalizedMessage())));
                return;
            }
            if (snapshot == null) return;
            List<DocumentSnapshot> docs = new ArrayList<>(snapshot.getDocuments());
            docs.sort(Comparator.comparingLong(this::lastMessageMillis).reversed());
            runOnUiThread(() -> renderThreads(docs));
        });
    }

    private void renderThreads(List<DocumentSnapshot> docs) {
        list.removeAllViews();
        if (docs.isEmpty()) {
            status.setText("No conversations yet");
            TextView empty = new TextView(this);
            empty.setText("Tap + New to find a user and start a private chat.");
            empty.setTextSize(16);
            empty.setTextColor(0xff666666);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(10), dp(50), dp(10), dp(30));
            list.addView(empty);
            return;
        }
        status.setText(docs.size() + (docs.size() == 1 ? " conversation" : " conversations"));
        for (DocumentSnapshot doc : docs) addThreadRow(doc);
    }

    @SuppressWarnings("unchecked")
    private void addThreadRow(DocumentSnapshot doc) {
        Object rawMembers = doc.get("members");
        if (!(rawMembers instanceof List)) return;
        List<Object> members = (List<Object>) rawMembers;
        String target = null;
        for (Object item : members) {
            if (item != null && !me.equals(String.valueOf(item))) { target = String.valueOf(item); break; }
        }
        if (target == null) return;

        String name = "KING User";
        Object rawNames = doc.get("memberNames");
        if (rawNames instanceof Map) {
            Object found = ((Map<?,?>) rawNames).get(target);
            if (found != null && !String.valueOf(found).trim().isEmpty()) name = String.valueOf(found);
        }

        long unread = 0;
        Object rawUnread = doc.get("unread");
        if (rawUnread instanceof Map) {
            Object value = ((Map<?,?>) rawUnread).get(me);
            if (value instanceof Number) unread = ((Number) value).longValue();
        }

        String preview = doc.getString("lastText");
        if (preview == null || preview.trim().isEmpty()) preview = "Start chatting";
        String time = formatTime(doc.getTimestamp("lastMessageAt"));
        String finalTarget = target;
        String finalName = name;

        Button row = new Button(this);
        row.setAllCaps(false);
        row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        row.setTextColor(Color.BLACK);
        row.setText(finalName + (unread > 0 ? "   • " + unread + " new" : "") + "\n" + preview + (time.isEmpty() ? "" : "   " + time));
        row.setOnClickListener(v -> {
            Intent i = new Intent(this, PrivateChatActivity.class);
            i.putExtra("targetUid", finalTarget);
            i.putExtra("targetName", finalName);
            startActivity(i);
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(78));
        lp.setMargins(0, dp(4), 0, dp(4));
        list.addView(row, lp);
    }

    private long lastMessageMillis(DocumentSnapshot doc) {
        Timestamp t = doc.getTimestamp("lastMessageAt");
        return t == null ? 0L : t.toDate().getTime();
    }

    private String formatTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        Date date = timestamp.toDate();
        return new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(date);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "unknown error" : value;
    }

    @Override protected void onDestroy() {
        if (listener != null) listener.remove();
        super.onDestroy();
    }
}
