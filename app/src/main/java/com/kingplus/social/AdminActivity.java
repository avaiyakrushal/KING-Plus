package com.kingplus.social;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

public class AdminActivity extends Activity {
    private final int BG = 0xff111522;
    private final int CARD = 0xff242b47;
    private final int PURPLE = 0xff7146ec;
    private LinearLayout page;
    private ListenerRegistration reportsListener;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { base("Admin Dashboard", "Firebase sign-in required"); text("Sign in with a real Firebase account first.", Color.WHITE); return; }
        base("Admin Dashboard", "Checking privileged admin claim…");
        user.getIdToken(true).addOnSuccessListener(token -> {
            boolean admin = Boolean.TRUE.equals(token.getClaims().get("admin"));
            if (!admin) {
                base("Admin Dashboard", "Access denied");
                text("This account does not have the Firebase custom claim admin=true. Normal users cannot read moderation reports or alter server wallets.", Color.WHITE);
                button("Close", CARD, v -> finish());
                return;
            }
            renderAdmin();
        }).addOnFailureListener(e -> toast("Admin check failed: " + safe(e.getLocalizedMessage())));
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private void base(String title, String subtitle) {
        if (reportsListener != null) { reportsListener.remove(); reportsListener = null; }
        ScrollView scroll = new ScrollView(this); scroll.setBackgroundColor(BG); scroll.setFillViewport(true);
        page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(20),dp(26),dp(20),dp(36)); scroll.addView(page); setContentView(scroll);
        TextView h = text(title, Color.WHITE); h.setTextSize(27); h.setTypeface(null, android.graphics.Typeface.BOLD);
        TextView s = text(subtitle, 0xffc5bfd8); s.setTextSize(14);
    }
    private TextView text(String value, int color) { TextView t=new TextView(this); t.setText(value); t.setTextColor(color); t.setTextSize(15); t.setPadding(dp(4),dp(8),dp(4),dp(8)); page.addView(t,new LinearLayout.LayoutParams(-1,-2)); return t; }
    private Button button(String value, int color, View.OnClickListener action) { Button b=new Button(this); b.setText(value); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setBackgroundColor(color); b.setOnClickListener(action); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52)); p.setMargins(0,dp(4),0,dp(4)); page.addView(b,p); return b; }
    private EditText input(String hint) { EditText e=new EditText(this); e.setHint(hint); e.setSingleLine(true); e.setTextColor(Color.WHITE); e.setHintTextColor(0xffaaa3bc); e.setBackgroundColor(CARD); e.setPadding(dp(12),0,dp(12),0); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52)); p.setMargins(0,dp(4),0,dp(4)); page.addView(e,p); return e; }

    private void renderAdmin() {
        base("Admin Dashboard", "Cloud moderation + bans + server wallet controls");
        text("Admin tools are protected by Firebase custom claim admin=true and trusted Cloud Functions.", 0xffffd768);
        EditText banUid = input("User UID to ban");
        EditText banReason = input("Ban reason");
        button("Ban user", PURPLE, v -> {
            String uid=banUid.getText().toString().trim(); String reason=banReason.getText().toString().trim();
            if(uid.isEmpty()){banUid.setError("UID required");return;}
            CloudBackend.banUser(uid,reason,(ok,m)->runOnUiThread(()->toast(m)));
        });
        EditText walletUid = input("Wallet user UID");
        EditText delta = input("Coin adjustment, e.g. 100 or -50");
        EditText walletReason = input("Adjustment reason");
        button("Apply server wallet adjustment", CARD, v -> {
            try {
                long amount=Long.parseLong(delta.getText().toString().trim());
                if(walletUid.getText().toString().trim().isEmpty()){walletUid.setError("UID required");return;}
                CloudBackend.adjustWallet(walletUid.getText().toString().trim(),amount,walletReason.getText().toString().trim(),(ok,m)->runOnUiThread(()->toast(m)));
            } catch(Exception ex){delta.setError("Enter a whole number");}
        });
        text("Moderation reports", Color.WHITE).setTextSize(20);
        LinearLayout list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); page.addView(list,new LinearLayout.LayoutParams(-1,-2));
        reportsListener = FirebaseFirestore.getInstance().collection("reports").orderBy("createdAt", Query.Direction.DESCENDING).limit(40)
            .addSnapshotListener((snap,error)->{
                if(error!=null){toast("Report load failed: "+safe(error.getLocalizedMessage()));return;}
                list.removeAllViews();
                if(snap==null||snap.isEmpty()){ TextView empty=new TextView(this); empty.setText("No moderation reports."); empty.setTextColor(0xffc5bfd8); empty.setPadding(dp(8),dp(10),dp(8),dp(10)); list.addView(empty); return; }
                for(DocumentSnapshot doc:snap.getDocuments()) addReportCard(list,doc);
            });
        button("Close dashboard", CARD, v -> finish());
    }

    private void addReportCard(LinearLayout list, DocumentSnapshot doc) {
        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(12),dp(10),dp(12),dp(10)); card.setBackgroundColor(CARD);
        String target=doc.getString("target"); String reason=doc.getString("reason"); String status=doc.getString("status");
        TextView info=new TextView(this); info.setText("Target: "+(target==null?"?":target)+"\nReason: "+(reason==null?"?":reason)+"\nStatus: "+(status==null?"new":status)); info.setTextColor(Color.WHITE); info.setTextSize(14); card.addView(info);
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL);
        Button resolve=new Button(this); resolve.setText("Resolve"); resolve.setAllCaps(false); resolve.setOnClickListener(v->CloudBackend.moderateReport(doc.getId(),"resolved",(ok,m)->runOnUiThread(()->toast(m))));
        Button dismiss=new Button(this); dismiss.setText("Dismiss"); dismiss.setAllCaps(false); dismiss.setOnClickListener(v->CloudBackend.moderateReport(doc.getId(),"dismissed",(ok,m)->runOnUiThread(()->toast(m))));
        actions.addView(resolve,new LinearLayout.LayoutParams(0,dp(48),1)); actions.addView(dismiss,new LinearLayout.LayoutParams(0,dp(48),1)); card.addView(actions);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,dp(5),0,dp(5)); list.addView(card,p);
    }

    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private String safe(String v){return v==null||v.trim().isEmpty()?"unknown error":v;}
    @Override protected void onDestroy(){if(reportsListener!=null)reportsListener.remove();super.onDestroy();}
}
