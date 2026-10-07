package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/** KING Plus original wardrobe. No billing and no third-party/reference assets. */
public class KingWardrobeActivity extends Activity {
    private static final int BG=0xfff7f5fb, INK=0xff292530, MUTED=0xff847d8c, PURPLE=0xff7b4bd4;
    private LinearLayout body;
    private LevelSystem.Snapshot progress;
    private FirebaseFirestore db;
    private FirebaseUser me;

    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(10),dp(8),dp(10),dp(8));return t;}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        progress=LevelSystem.read(this);
        try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Throwable ignored){}
        render();
    }

    private void render(){
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(BG);
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(16),dp(18),dp(16),dp(30));sc.addView(body);
        TextView back=tv("‹  Wardrobe",26,INK,true);back.setOnClickListener(v->finish());body.addView(back,new LinearLayout.LayoutParams(-1,dp(58)));
        TextView sub=tv("Equip KING Plus frames and room entrance effects. No billing.",13,MUTED,false);body.addView(sub);
        addPreview();
        section("Profile Frames");
        for(String f:KingCosmetics.FRAMES)addFrame(f);
        section("Entrance Effects");
        for(String e:KingCosmetics.EFFECTS)addEffect(e);
        section("Chat Bubbles");
        for(String x:KingCosmetics.BUBBLES)addBubble(x);
        section("Name Badges");
        for(String x:KingCosmetics.BADGES)addBadge(x);
        TextView note=tv("Unlock cosmetics by normal Level / VIP progress. Equipped cosmetics sync to your public profile and Party room identity.",12,MUTED,false);
        note.setBackground(bg(Color.WHITE,14));LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.setMargins(0,dp(14),0,0);body.addView(note,np);
        setContentView(sc);
    }

    private void addPreview(){
        String frame=KingCosmetics.frame(this),effect=KingCosmetics.effect(this);
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(12),dp(12),dp(12),dp(12));
        card.setBackground(KingCosmetics.idFrame(this,frame));
        TextView avatar=tv(KingCosmetics.frameEmoji(frame)+"  KING  "+KingCosmetics.effectEmoji(effect),26,Color.WHITE,true);avatar.setGravity(Gravity.CENTER);card.addView(avatar,new LinearLayout.LayoutParams(-1,dp(62)));
        TextView cur=tv("Equipped: "+frame+"  •  "+effect,13,Color.WHITE,true);cur.setGravity(Gravity.CENTER);card.addView(cur,new LinearLayout.LayoutParams(-1,dp(42)));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(112));cp.setMargins(0,dp(6),0,dp(14));body.addView(card,cp);
    }

    private void section(String title){TextView h=tv(title,18,INK,true);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48));p.setMargins(0,dp(6),0,dp(2));body.addView(h,p);}

    private void addFrame(String name){
        boolean unlocked=KingCosmetics.unlockedFrame(name,progress),equipped=name.equals(KingCosmetics.frame(this));
        TextView row=tv((equipped?"✓  ":unlocked?"◇  ":"🔒  ")+KingCosmetics.frameEmoji(name)+"  "+name+"\n"+(equipped?"Equipped":unlocked?"Tap to equip":"Unlock at "+KingCosmetics.requirementFrame(name)),15,equipped?Color.WHITE:INK,true);
        row.setBackground(bg(equipped?PURPLE:Color.WHITE,14));
        row.setOnClickListener(v->{if(!unlocked){toast("Locked • "+KingCosmetics.requirementFrame(name));return;}KingCosmetics.setFrame(this,name);syncCloud();toast(name+" equipped");render();});
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(68));p.setMargins(0,dp(4),0,dp(4));body.addView(row,p);
    }

    private void addEffect(String name){
        boolean unlocked=KingCosmetics.unlockedEffect(name,progress),equipped=name.equals(KingCosmetics.effect(this));
        TextView row=tv((equipped?"✓  ":unlocked?"✨  ":"🔒  ")+KingCosmetics.effectEmoji(name)+"  "+name+"\n"+(equipped?"Equipped":unlocked?"Tap to equip":"Unlock at "+KingCosmetics.requirementEffect(name)),15,equipped?Color.WHITE:INK,true);
        row.setBackground(bg(equipped?0xff8f55c5:Color.WHITE,14));
        row.setOnClickListener(v->{if(!unlocked){toast("Locked • "+KingCosmetics.requirementEffect(name));return;}KingCosmetics.setEffect(this,name);syncCloud();toast(name+" equipped");render();});
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(68));p.setMargins(0,dp(4),0,dp(4));body.addView(row,p);
    }

    private void addBubble(String name){
        boolean unlocked=KingCosmetics.unlockedBubble(name,progress),equipped=name.equals(KingCosmetics.bubble(this));
        TextView row=tv((equipped?"✓  ":unlocked?"💬  ":"🔒  ")+KingCosmetics.bubbleEmoji(name)+"  "+name+"\n"+(equipped?"Equipped":unlocked?"Tap to equip":"Unlock at "+KingCosmetics.requirementBubble(name)),15,equipped?Color.WHITE:INK,true);
        row.setBackground(bg(equipped?KingCosmetics.bubbleColor(this):Color.WHITE,14));
        row.setOnClickListener(v->{if(!unlocked){toast("Locked • "+KingCosmetics.requirementBubble(name));return;}KingCosmetics.setBubble(this,name);syncCloud();toast(name+" equipped");render();});
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(68));p.setMargins(0,dp(4),0,dp(4));body.addView(row,p);
    }

    private void addBadge(String name){
        boolean unlocked=KingCosmetics.unlockedBadge(name,progress),equipped=name.equals(KingCosmetics.badge(this));
        TextView row=tv((equipped?"✓  ":unlocked?"🏷  ":"🔒  ")+KingCosmetics.badgeEmoji(name)+"  "+name+"\n"+(equipped?"Equipped":unlocked?"Tap to equip":"Unlock at "+KingCosmetics.requirementBadge(name)),15,equipped?Color.WHITE:INK,true);
        row.setBackground(bg(equipped?0xff6650aa:Color.WHITE,14));
        row.setOnClickListener(v->{if(!unlocked){toast("Locked • "+KingCosmetics.requirementBadge(name));return;}KingCosmetics.setBadge(this,name);syncCloud();toast(name+" equipped");render();});
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(68));p.setMargins(0,dp(4),0,dp(4));body.addView(row,p);
    }

    private void syncCloud(){
        if(db==null||me==null)return;
        Map<String,Object> m=new HashMap<>();
        m.put("uid",me.getUid());
        m.put("equippedFrame",KingCosmetics.frame(this));
        m.put("entranceEffect",KingCosmetics.effect(this));
        m.put("chatBubble",KingCosmetics.bubble(this));
        m.put("nameBadge",KingCosmetics.badge(this));
        m.put("level",progress.level);m.put("vipLevel",progress.vipLevel);
        m.put("updatedAt",FieldValue.serverTimestamp());
        db.collection("public_profiles").document(me.getUid()).set(m,SetOptions.merge()).addOnFailureListener(e->{});
    }
}
