package com.kingplus.social;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

/** Basic native controls for KING Plus voice-room seat state. */
public class RoomControlActivity extends Activity {
    private final RoomSeatManager seats = new RoomSeatManager();
    private String roomId;
    private String uid;
    private String name;
    private int mySeat = 1;
    private boolean muted = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        roomId = getIntent().getStringExtra("roomId");
        if (roomId == null || roomId.trim().isEmpty()) roomId = "lobby";
        uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) uid = "guest-" + System.currentTimeMillis();
        name = getIntent().getStringExtra("name");
        if (name == null || name.trim().isEmpty()) name = "KING User";

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32,48,32,32);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView title = new TextView(this);
        title.setText("KING Plus Voice Room\n" + roomId);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1,-2));

        add(root,"Request Seat", v -> seats.requestSeat(roomId,uid,name,this::result));
        add(root,"Join Seat 1", v -> seats.setSeat(roomId,mySeat,uid,name,muted,this::result));
        add(root,"Mic Mute / Unmute", v -> { muted=!muted; seats.setMuted(roomId,mySeat,muted,this::result); });
        add(root,"Leave Seat", v -> seats.leaveSeat(roomId,mySeat,this::result));
        add(root,"Host: Lock Seat 1", v -> seats.lockSeat(roomId,mySeat,true,this::result));
        add(root,"Host: Unlock Seat 1", v -> seats.lockSeat(roomId,mySeat,false,this::result));
        add(root,"Make Me Co-host (test)", v -> seats.setRole(roomId,uid,"cohost",this::result));
        add(root,"Back", v -> finish());
        setContentView(root);
    }

    private void add(LinearLayout root, String text, View.OnClickListener click) {
        Button b=new Button(this); b.setText(text); b.setOnClickListener(click);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.topMargin=16; root.addView(b,p);
    }
    private void result(boolean ok,String message) {
        runOnUiThread(() -> Toast.makeText(this,(ok?"✓ ":"⚠ ") + message,Toast.LENGTH_SHORT).show());
    }
}
