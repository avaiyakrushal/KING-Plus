package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.firestore.Source;
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
    private String category="All";
    private boolean obtainedOnly=false,loading=true,saving=false,profileLoaded=false;
    private String equippedFrame="Minimal Frame",equippedEffect="None",equippedBubble="Classic Purple",equippedBadge="None";
    private String status="Loading verified account collection…";

    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(10),dp(8),dp(10),dp(8));return t;}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        progress=new LevelSystem.Snapshot(1,0,0,0);
        try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Throwable ignored){}
        String requested=getIntent().getStringExtra("category");
        if("Frames".equals(requested)||"Entrances".equals(requested)||"Bubbles".equals(requested)||"Badges".equals(requested))category=requested;
        obtainedOnly=getIntent().getBooleanExtra("obtained",false);
        render();loadCollection();
    }

    private boolean activeAccount(){
        FirebaseUser current=FirebaseAuth.getInstance().getCurrentUser();
        return !isFinishing()&&!isDestroyed()&&me!=null&&current!=null&&!current.isAnonymous()&&me.getUid().equals(current.getUid());
    }

    private void loadCollection(){
        if(db==null||!activeAccount()){loading=false;status="Sign in to a permanent KING account to manage cosmetics.";render();return;}
        loading=true;profileLoaded=false;status="Loading verified account collection…";render();
        db.collection("public_profiles").document(me.getUid()).get(Source.SERVER).addOnSuccessListener(doc->{
            if(!activeAccount())return;
            equippedFrame=known(doc.getString("equippedFrame"),KingCosmetics.FRAMES,"Minimal Frame");
            equippedEffect=known(doc.getString("entranceEffect"),KingCosmetics.EFFECTS,"None");
            equippedBubble=known(doc.getString("chatBubble"),KingCosmetics.BUBBLES,"Classic Purple");
            equippedBadge=known(doc.getString("nameBadge"),KingCosmetics.BADGES,"None");
            profileLoaded=true;
            FirebaseFunctions.getInstance().getHttpsCallable("getDailyCheckinStatus").call()
                .addOnSuccessListener(result->{
                    if(!activeAccount())return;
                    Object data=result.getData();
                    if(!(data instanceof Map)){loadFailed();return;}
                    Map<?,?> m=(Map<?,?>)data;
                    int level=number(m.get("activityLevel"),1),xp=number(m.get("activityXp"),0);
                    // Paid VIP cosmetics remain disabled until a dedicated server purchase/entitlement flow exists.
                    progress=new LevelSystem.Snapshot(Math.max(1,Math.min(99,level)),xp,0,0);
                    loading=false;status="Verified Activity Lv."+progress.level+" • tap an item to preview or equip";render();
                }).addOnFailureListener(e->loadFailed());
        }).addOnFailureListener(e->loadFailed());
    }
    private void loadFailed(){if(!activeAccount())return;loading=false;profileLoaded=false;progress=new LevelSystem.Snapshot(1,0,0,0);status="Collection could not be verified online. Tap Retry.";render();}
    private int number(Object value,int fallback){return value instanceof Number?Math.max(0,((Number)value).intValue()):fallback;}
    private String known(String value,String[] options,String fallback){for(String option:options)if(option.equals(value))return option;return fallback;}

    private void render(){
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(BG);
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(12),dp(10),dp(12),dp(24));sc.addView(body);
        TextView back=tv("‹  "+(obtainedOnly?"My Privilege Pack":"Privilege Shop"),22,INK,true);back.setGravity(Gravity.CENTER_VERTICAL);back.setOnClickListener(v->finish());body.addView(back,new LinearLayout.LayoutParams(-1,dp(52)));
        LinearLayout modes=new LinearLayout(this);
        for(String mode:new String[]{"All items","Obtained"}){boolean obtained=mode.equals("Obtained");TextView tab=tv(mode,14,obtained==obtainedOnly?Color.WHITE:INK,true);tab.setGravity(Gravity.CENTER);tab.setBackground(bg(obtained==obtainedOnly?PURPLE:Color.WHITE,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(48),1);lp.setMargins(dp(3),0,dp(3),0);modes.addView(tab,lp);tab.setOnClickListener(v->{obtainedOnly=obtained;render();});}
        body.addView(modes);
        TextView note=tv(status,12,MUTED,false);note.setOnClickListener(v->{if(!loading&&!saving)loadCollection();});body.addView(note);
        addPreview();
        HorizontalScrollView scroll=new HorizontalScrollView(this);scroll.setHorizontalScrollBarEnabled(false);LinearLayout tabs=new LinearLayout(this);
        for(String c:new String[]{"All","Frames","Entrances","Bubbles","Badges"}){TextView tab=tv(c,13,c.equals(category)?Color.WHITE:INK,true);tab.setGravity(Gravity.CENTER);tab.setBackground(bg(c.equals(category)?PURPLE:Color.WHITE,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(44));lp.setMargins(dp(3),0,dp(3),0);tabs.addView(tab,lp);tab.setOnClickListener(v->{category=c;render();});}
        scroll.addView(tabs);body.addView(scroll);
        if(!obtainedOnly||profileLoaded){
            if("All".equals(category)||"Frames".equals(category)){section("Avatar Frames");for(String f:KingCosmetics.FRAMES)addFrame(f);}
            if("All".equals(category)||"Entrances".equals(category)){section("Entrance Effects");for(String e:KingCosmetics.EFFECTS)addEffect(e);}
            if("All".equals(category)||"Bubbles".equals(category)){section("Chat Bubbles");for(String x:KingCosmetics.BUBBLES)addBubble(x);}
            if("All".equals(category)||"Badges".equals(category)){section("Name Badges");for(String x:KingCosmetics.BADGES)addBadge(x);}
        }
        body.addView(tv("Obtained shows currently eligible free items, not a paid purchase history. Diamond purchases and additional privilege types are not available yet.",12,MUTED,false));
        setContentView(sc);
        ViewCompat.setOnApplyWindowInsetsListener(sc,(v,insets)->{Insets area=insets.getInsets(WindowInsetsCompat.Type.systemBars()|WindowInsetsCompat.Type.displayCutout());v.setPadding(area.left,area.top,area.right,area.bottom);return insets;});ViewCompat.requestApplyInsets(sc);
    }

    private void addPreview(){
        String frame=equippedFrame,effect=equippedEffect;
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(12),dp(12),dp(12),dp(12));
        card.setBackground(KingCosmetics.idFrame(this,frame));
        TextView avatar=tv(KingCosmetics.frameEmoji(frame)+"  KING  "+KingCosmetics.effectEmoji(effect),26,Color.WHITE,true);avatar.setGravity(Gravity.CENTER);card.addView(avatar,new LinearLayout.LayoutParams(-1,dp(62)));
        TextView cur=tv("Equipped: "+frame+"  •  "+effect,13,Color.WHITE,true);cur.setGravity(Gravity.CENTER);card.addView(cur,new LinearLayout.LayoutParams(-1,dp(42)));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(112));cp.setMargins(0,dp(6),0,dp(14));body.addView(card,cp);
    }

    private void section(String title){TextView h=tv(title,18,INK,true);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48));p.setMargins(0,dp(6),0,dp(2));body.addView(h,p);}

    private void addFrame(String name){addItem("equippedFrame",name,KingCosmetics.frameEmoji(name),KingCosmetics.requirementFrame(name),KingCosmetics.unlockedFrame(name,progress),name.equals(equippedFrame));}
    private void addEffect(String name){addItem("entranceEffect",name,KingCosmetics.effectEmoji(name),KingCosmetics.requirementEffect(name),KingCosmetics.unlockedEffect(name,progress),name.equals(equippedEffect));}
    private void addBubble(String name){addItem("chatBubble",name,KingCosmetics.bubbleEmoji(name),KingCosmetics.requirementBubble(name),KingCosmetics.unlockedBubble(name,progress),name.equals(equippedBubble));}
    private void addBadge(String name){addItem("nameBadge",name,KingCosmetics.badgeEmoji(name),KingCosmetics.requirementBadge(name),KingCosmetics.unlockedBadge(name,progress),name.equals(equippedBadge));}

    private void addItem(String field,String name,String icon,String requirement,boolean unlocked,boolean equipped){
        if(obtainedOnly&&!unlocked)return;
        TextView row=tv((equipped?"✓ ":unlocked?"◇ ":"🔒 ")+icon+" "+name+"\n"+(equipped?"Equipped":unlocked?"Preview / Equip":"Requires "+requirement),14,equipped?Color.WHITE:INK,true);
        row.setGravity(Gravity.CENTER_VERTICAL);row.setBackground(bg(equipped?PURPLE:Color.WHITE,14));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(4),0,dp(4));row.setMinHeight(dp(60));body.addView(row,p);
        row.setOnClickListener(v->{
            TextView preview=tv(icon+"  "+name+"\n"+(unlocked?"Available":"Locked • "+requirement),18,INK,true);
            preview.setGravity(Gravity.CENTER);preview.setMinHeight(dp(112));
            if("equippedFrame".equals(field))preview.setBackground(KingCosmetics.idFrame(this,name));
            if("chatBubble".equals(field)){preview.setBackground(bg(KingCosmetics.bubbleColor(name),16));preview.setTextColor(Color.WHITE);}
            AlertDialog.Builder dialog=new AlertDialog.Builder(this).setTitle("Preview").setView(preview).setNegativeButton("Close",null);
            if(unlocked&&!equipped&&profileLoaded&&!loading&&!saving)dialog.setPositiveButton("Equip",(di,which)->saveItem(field,name));
            dialog.show();
        });
    }

    private void saveItem(String field,String name){
        if(!activeAccount()||!profileLoaded||loading||saving)return;
        boolean allowed="equippedFrame".equals(field)?KingCosmetics.unlockedFrame(name,progress):
            "entranceEffect".equals(field)?KingCosmetics.unlockedEffect(name,progress):
            "chatBubble".equals(field)?KingCosmetics.unlockedBubble(name,progress):
            "nameBadge".equals(field)&&KingCosmetics.unlockedBadge(name,progress);
        if(!allowed)return;
        saving=true;status="Saving "+name+"…";render();
        Map<String,Object> m=new HashMap<>();m.put("uid",me.getUid());m.put(field,name);m.put("updatedAt",FieldValue.serverTimestamp());
        // Never overwrite server-managed VIP/Noble entitlement fields during equip.
        db.collection("public_profiles").document(me.getUid()).set(m,SetOptions.merge()).addOnSuccessListener(v->{
            if(!activeAccount())return;
            if("equippedFrame".equals(field)){equippedFrame=name;KingCosmetics.setFrame(this,name);}
            else if("entranceEffect".equals(field)){equippedEffect=name;KingCosmetics.setEffect(this,name);}
            else if("chatBubble".equals(field)){equippedBubble=name;KingCosmetics.setBubble(this,name);}
            else if("nameBadge".equals(field)){equippedBadge=name;KingCosmetics.setBadge(this,name);}
            saving=false;status=name+" • synced to your account";render();
        }).addOnFailureListener(e->{if(!activeAccount())return;saving=false;status="Cloud sync failed. Previous equipped item retained. Tap to retry.";render();});
    }
}
