package com.kingplus.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/** Find a KING Plus user by Firebase UID and open follow/friend/private-message actions. */
public class UserSocialActivity extends Activity {
    private final SocialManager social = new SocialManager();
    private String me;
    private LinearLayout root;
    private EditText search;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        me = FirebaseAuth.getInstance().getUid();
        if (me == null) { finish(); return; }
        renderSearch();
    }

    private void renderSearch() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 32);

        TextView title = new TextView(this);
        title.setText("Find KING Plus User");
        title.setTextSize(24);
        root.addView(title);

        TextView help = new TextView(this);
        help.setText("Enter another user's Firebase UID to start a private chat.");
        root.addView(help);

        search = new EditText(this);
        search.setHint("Firebase UID");
        root.addView(search);

        add("Search User", () -> find(search.getText().toString().trim()));
        add("Open Message Inbox", () -> startActivity(new Intent(this, ChatInboxActivity.class)));
        setContentView(root);
    }

    private void find(String uid) {
        if (uid.isEmpty() || uid.equals(me)) {
            Toast.makeText(this, "Enter another user's UID", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore.getInstance().collection("public_profiles").document(uid).get()
            .addOnSuccessListener(doc -> showUser(uid, doc))
            .addOnFailureListener(error -> showUser(uid, null));
    }

    private void showUser(String uid, DocumentSnapshot doc) {
        root.removeAllViews();
        String name = doc == null ? null : doc.getString("displayName");
        if (name == null || name.trim().isEmpty()) name = "KING User";
        final String finalName = name;

        TextView title = new TextView(this);
        title.setText(finalName + "\nUID: " + uid);
        title.setTextSize(22);
        root.addView(title);

        add("Private Message", () -> {
            Intent i = new Intent(this, PrivateChatActivity.class);
            i.putExtra("targetUid", uid);
            i.putExtra("targetName", finalName);
            startActivity(i);
        });
        add("Follow", () -> social.follow(me, uid, this::result));
        add("Unfollow", () -> social.unfollow(me, uid, this::result));
        add("Add Friend", () -> social.addFriend(me, uid, this::result));
        add("Search Another User", this::renderSearch);
    }

    private void add(String text, Runnable action) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setOnClickListener(v -> action.run());
        root.addView(button, new LinearLayout.LayoutParams(-1, -2));
    }

    private void result(boolean ok, String message) {
        runOnUiThread(() -> Toast.makeText(this, (ok ? "✓ " : "⚠ ") + message, Toast.LENGTH_SHORT).show());
    }
}
