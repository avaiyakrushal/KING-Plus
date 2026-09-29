package com.kingplus.social;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Real-time one-to-one KING Plus chat backed by Firestore direct_threads. */
public class PrivateChatActivity extends Activity {
    private final SocialManager social = new SocialManager();
    private String me;
    private String target;
    private String targetName;
    private FirebaseUser user;
    private LinearLayout messages;
    private ScrollView scroll;
    private EditText input;
    private Button send;
    private ListenerRegistration listener;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }

        me = user.getUid();
        target = getIntent().getStringExtra("targetUid");
        targetName = getIntent().getStringExtra("targetName");
        if (target == null || target.trim().isEmpty() || target.equals(me)) {
            Toast.makeText(this, "Invalid chat user", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (targetName == null || targetName.trim().isEmpty()) targetName = "KING User";

        render();
        startMessageListener();
        social.markThreadRead(me, target);
        PushNotifications.initialize(this);
    }

    private void render() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xfff7f7fb);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(12), dp(12), dp(12), dp(10));

        Button back = new Button(this);
        back.setText("‹");
        back.setTextSize(26);
        back.setAllCaps(false);
        back.setOnClickListener(v -> finish());
        header.addView(back, new LinearLayout.LayoutParams(dp(54), dp(54)));

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setText(targetName);
        title.setTextSize(20);
        title.setTextColor(Color.BLACK);
        TextView subtitle = new TextView(this);
        subtitle.setText("Private chat • encrypted in transit by Firebase");
        subtitle.setTextSize(12);
        subtitle.setTextColor(0xff666666);
        titles.addView(title);
        titles.addView(subtitle);
        header.addView(titles, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(header);

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(dp(12), dp(8), dp(12), dp(8));
        scroll.addView(messages);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout composer = new LinearLayout(this);
        composer.setGravity(Gravity.BOTTOM);
        composer.setPadding(dp(10), dp(8), dp(10), dp(12));

        input = new EditText(this);
        input.setHint("Write a message…");
        input.setSingleLine(false);
        input.setMaxLines(4);
        input.setImeOptions(EditorInfo.IME_ACTION_SEND);
        composer.addView(input, new LinearLayout.LayoutParams(0, -2, 1));

        send = new Button(this);
        send.setText("Send");
        send.setAllCaps(false);
        send.setOnClickListener(v -> sendCurrentMessage());
        composer.addView(send, new LinearLayout.LayoutParams(dp(92), -2));
        root.addView(composer);

        setContentView(root);
    }

    private void startMessageListener() {
        listener = social.listenMessages(me, target, (snapshot, error) -> {
            if (error != null) {
                runOnUiThread(() -> Toast.makeText(this, "Chat sync failed: " + safe(error.getLocalizedMessage()), Toast.LENGTH_LONG).show());
                return;
            }
            if (snapshot == null) return;
            runOnUiThread(() -> {
                messages.removeAllViews();
                if (snapshot.isEmpty()) addEmptyState();
                for (DocumentSnapshot doc : snapshot.getDocuments()) addMessage(doc);
                social.markThreadRead(me, target);
                scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
            });
        });
    }

    private void sendCurrentMessage() {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) return;
        if (text.length() > 1000) {
            input.setError("Maximum 1000 characters");
            return;
        }
        send.setEnabled(false);
        social.sendMessage(me, target, safeName(), targetName, text, (ok, message) -> runOnUiThread(() -> {
            send.setEnabled(true);
            if (ok) input.setText("");
            else Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }));
    }

    private void addEmptyState() {
        TextView row = new TextView(this);
        row.setText("No messages yet. Say hello 👋");
        row.setTextColor(0xff777777);
        row.setGravity(Gravity.CENTER);
        row.setPadding(dp(8), dp(40), dp(8), dp(24));
        messages.addView(row, new LinearLayout.LayoutParams(-1, -2));
    }

    private void addMessage(DocumentSnapshot doc) {
        String sender = doc.getString("senderUid");
        String senderName = doc.getString("senderName");
        String text = doc.getString("text");
        boolean mine = me.equals(sender);

        LinearLayout wrap = new LinearLayout(this);
        wrap.setGravity(mine ? Gravity.END : Gravity.START);
        wrap.setPadding(dp(4), dp(4), dp(4), dp(4));

        TextView bubble = new TextView(this);
        String label = mine ? "You" : (senderName == null || senderName.trim().isEmpty() ? targetName : senderName);
        bubble.setText(label + "\n" + (text == null ? "" : text) + "\n" + formatTime(doc.getTimestamp("createdAt")));
        bubble.setTextSize(15);
        bubble.setTextColor(mine ? Color.WHITE : 0xff202020);
        bubble.setBackgroundColor(mine ? 0xff7146ec : 0xffe9e9ef);
        bubble.setPadding(dp(14), dp(10), dp(14), dp(10));
        wrap.addView(bubble, new LinearLayout.LayoutParams(-2, -2));
        messages.addView(wrap, new LinearLayout.LayoutParams(-1, -2));
    }

    private String formatTime(Timestamp timestamp) {
        Date date = timestamp == null ? new Date() : timestamp.toDate();
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
    }

    private String safeName() {
        String name = user.getDisplayName();
        return name == null || name.trim().isEmpty() ? "KING User" : name.trim();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "unknown error" : value;
    }

    @Override protected void onResume() {
        super.onResume();
        if (me != null && target != null) social.markThreadRead(me, target);
    }

    @Override protected void onDestroy() {
        if (listener != null) listener.remove();
        super.onDestroy();
    }
}
