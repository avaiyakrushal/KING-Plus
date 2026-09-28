package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/** Native controls for KING Plus voice-room seats and host moderation. */
public class RoomControlActivity extends Activity {
    private final RoomSeatManager seats = new RoomSeatManager();
    private String roomId, uid, name, ownerUid;
    private int mySeat = 1;
    private boolean muted = false;
    private boolean isHost = false;
    private boolean isCoHost = false;
    private LinearLayout root;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        roomId = getIntent().getStringExtra("roomId");
        if (roomId == null || roomId.trim().isEmpty()) roomId = "lobby";
        uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) uid = "guest-" + System.currentTimeMillis();
        name = getIntent().getStringExtra("name");
        if (name == null || name.trim().isEmpty()) name = "KING User";
        ownerUid = getIntent().getStringExtra("ownerUid");
        isHost = ownerUid != null && ownerUid.equals(uid);
        loadRoleAndRender();
    }

    private void loadRoleAndRender() {
        FirebaseFirestore.getInstance().collection("live_rooms").document(roomId).collection("roles").document(uid).get()
            .addOnSuccessListener(doc -> {
                String role = doc.getString("role");
                isCoHost = "cohost".equals(role) || "admin".equals(role);
                render();
            }).addOnFailureListener(e -> render());
    }

    private void render() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32,48,32,32);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView title = new TextView(this);
        title.setText("KING Plus Voice Room\n" + roomId + "\nRole: " + (isHost ? "Host" : isCoHost ? "Co-host" : "Member"));
        title.setTextSize(22); title.setGravity(Gravity.CENTER);
        root.addView(title,new LinearLayout.LayoutParams(-1,-2));

        add("Request Seat", v -> seats.requestSeat(roomId,uid,name,this::result));
        add("Join Seat 1", v -> seats.setSeat(roomId,mySeat,uid,name,muted,this::result));
        add("Mic Mute / Unmute", v -> { muted=!muted; seats.setMuted(roomId,mySeat,muted,this::result); });
        add("Leave Seat", v -> seats.leaveSeat(roomId,mySeat,this::result));

        if (isHost || isCoHost) {
            add("Lock Seat 1", v -> seats.lockSeat(roomId,mySeat,true,this::result));
            add("Unlock Seat 1", v -> seats.lockSeat(roomId,mySeat,false,this::result));
            add("Kick / Ban User", v -> moderationDialog());
        }
        if (isHost) add("Assign Co-host", v -> roleDialog());
        add("Back", v -> finish());
        setContentView(root);
    }

    private void moderationDialog() {
        EditText target = new EditText(this); target.setHint("Firebase UID to ban");
        new AlertDialog.Builder(this).setTitle("Kick / Ban from room").setView(target)
            .setNegativeButton("Cancel",null).setPositiveButton("Ban",(d,w)->{
                String id=target.getText().toString().trim();
                if (id.isEmpty() || id.equals(uid)) { result(false,"Enter another user's UID"); return; }
                seats.banFromRoom(roomId,id,"Host moderation",this::result);
            }).show();
    }

    private void roleDialog() {
        EditText target = new EditText(this); target.setHint("Firebase UID for co-host");
        new AlertDialog.Builder(this).setTitle("Assign Co-host").setView(target)
            .setNegativeButton("Cancel",null).setPositiveButton("Assign",(d,w)->{
                String id=target.getText().toString().trim();
                if (id.isEmpty()) { result(false,"Enter user UID"); return; }
                seats.setRole(roomId,id,"cohost",this::result);
            }).show();
    }

    private void add(String text, View.OnClickListener click) {
        Button b=new Button(this); b.setText(text); b.setOnClickListener(click);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.topMargin=16; root.addView(b,p);
    }
    private void result(boolean ok,String message) {
        runOnUiThread(() -> Toast.makeText(this,(ok?"✓ ":"⚠ ") + message,Toast.LENGTH_SHORT).show());
    }
}
