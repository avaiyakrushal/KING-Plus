package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
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

/** Real preview & equip of KING Original frames and entrance effects.
 * Free entitlements are normal experience levels; paid VIP never comes
 * from fake local/test points. Cloud profile save is acknowledged first.
 */
public final class KingFrameGallery981Activity extends Activity {
    private boolean showingFrames=true,saving;
    private LinearLayout catalog;
    private TextView status;
    private int normalLevel;
    private int verifiedVipLevel=0; // no paid backend entitlement until verified.
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable panel(int color,int radius){
        GradientDrawable d=new GradientDrawable();
        d.setColor(color);d.setCornerRadius(dp(radius));return d;
    }
    private TextView text(String value,int size,int color,boolean strong){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);
        if(strong)t.setTypeface(null,Typeface.BOLD);
        t.setPadding(dp(12),dp(8),dp(12),dp(8));return t;
    }
    private String ownName(){
        FirebaseUser me=FirebaseAuth.getInstance().getCurrentUser();
        if(me==null)return "KING";
        String n=me.getDisplayName();
        if(n!=null&&!n.trim().isEmpty())return n.trim();
        return "KING";
    }
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        showingFrames=getIntent()==null||getIntent().getBooleanExtra("frames",true);
        normalLevel=LevelSystem.read(this).level;
        render();
    }
    private void render(){
        ScrollView sc=new ScrollView(this);
        LinearLayout page=new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(15),dp(12),dp(15),dp(32));
        page.setBackgroundColor(0xff161129);sc.addView(page);
        TextView title=text("‹   🖼 KING Frames & Entrance Effects",21,Color.WHITE,true);
        title.setOnClickListener(v->finish());page.addView(title);
        TextView help=text("Original visual designs • save a frame to show it on your profile and Party seat",13,0xffc9c2db,false);
        page.addView(help);
        status=text("Normal Level "+normalLevel+
            " • Premium VIP awaits verified Diamond Recharge",12,0xffffd479,true);
        page.addView(status);
        LinearLayout tabs=new LinearLayout(this);
        tabs.setGravity(Gravity.CENTER);
        TextView frames=text("🖼 Frames",15,Color.WHITE,true);
        TextView effects=text("✨ Entrance FX",15,Color.WHITE,true);
        frames.setGravity(Gravity.CENTER);
        effects.setGravity(Gravity.CENTER);
        frames.setBackground(panel(showingFrames?0xff8650dd:0xff302642,12));
        effects.setBackground(panel(showingFrames?0xff302642:0xff8650dd,12));
        tabs.addView(frames,new LinearLayout.LayoutParams(0,dp(54),1));
        LinearLayout.LayoutParams r=new LinearLayout.LayoutParams(0,dp(54),1);
        r.leftMargin=dp(8);tabs.addView(effects,r);page.addView(tabs);
        frames.setOnClickListener(v->{if(!showingFrames){showingFrames=true;render();}});
        effects.setOnClickListener(v->{if(showingFrames){showingFrames=false;render();}});
        catalog=new LinearLayout(this);catalog.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);
        cp.topMargin=dp(12);page.addView(catalog,cp);

        final String[] items=showingFrames?KingCosmetics.FRAMES:KingCosmetics.EFFECTS;
        final String current=showingFrames?KingCosmetics.frame(this):KingCosmetics.effect(this);
        for(String item:items)createCard(item,current);
        TextView notes=text("Free Frames unlock by normal gameplay level. Recharge VIP rewards stay locked until a trusted payment backend has actually verified your purchase. No paid upgrade is charged here.",12,0xffbbb4cc,false);
        LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);
        np.topMargin=dp(12);page.addView(notes,np);
        setContentView(sc);
    }
    private void createCard(String name,String equipped){
        final boolean can=showingFrames?
            KingPremiumAccess981.allowedFrame(name,normalLevel,verifiedVipLevel):
            KingPremiumAccess981.allowedEffect(name,normalLevel,verifiedVipLevel);
        final String requirement=showingFrames?
            KingCosmetics.requirementFrame(name):KingCosmetics.requirementEffect(name);
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(9),dp(9),dp(9),dp(9));
        card.setBackground(panel(0xff2b2444,16));
        FrameLayout preview=new FrameLayout(this);
        if(showingFrames){
            preview.setBackground(KingCosmetics.avatarFrame(this,name,false));
            TextView inner=text("👑",26,0xff1b1530,true);
            inner.setGravity(Gravity.CENTER);
            inner.setBackground(panel(0xfff0ecff,60));
            FrameLayout.LayoutParams img=new FrameLayout.LayoutParams(dp(57),dp(57),Gravity.CENTER);
            preview.addView(inner,img);
        }else{
            TextView art=text(KingCosmetics.effectEmoji(name),35,Color.WHITE,true);
            art.setGravity(Gravity.CENTER);
            art.setBackground(KingCosmetics.idFrame(this,"Galaxy Frame"));
            preview.addView(art,new FrameLayout.LayoutParams(-1,-1));
        }
        card.addView(preview,new LinearLayout.LayoutParams(dp(77),dp(77)));
        LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);
        TextView header=text(name,16,Color.WHITE,true);
        header.setPadding(dp(10),dp(3),0,dp(2));content.addView(header);
        String suffix=name.equals(equipped)?"✓ EQUIPPED":
            can?"✓ TAP TO EQUIP":"🔒 LOCKED";
        TextView state=text(requirement+" • "+suffix,12,
            can?0xff80edbb:0xffffca88,false);
        state.setPadding(dp(10),dp(2),0,0);content.addView(state);
        card.addView(content,new LinearLayout.LayoutParams(0,-2,1));
        card.setOnClickListener(v->{
            if(saving){Toast.makeText(this,"Saving your frame…",Toast.LENGTH_SHORT).show();return;}
            if(!can){
                String msg=!("Free".equals(requirement)||requirement.startsWith("Lv."))?
                    KingPremiumAccess981.paidVipMessage(name):
                    "Reach "+requirement+" by playing and using KING Plus";
                new AlertDialog.Builder(this).setTitle(name+" locked")
                    .setMessage(msg).setPositiveButton("OK",null).show();return;
            }
            if(name.equals(equipped)){Toast.makeText(this,"Already equipped",Toast.LENGTH_SHORT).show();return;}
            equip(name);
        });
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(97));
        lp.bottomMargin=dp(9);catalog.addView(card,lp);
    }
    private void equip(String selected){
        final boolean frames=showingFrames;
        final FirebaseUser account=FirebaseAuth.getInstance().getCurrentUser();
        if(account==null){
            applyLocal(frames,selected);
            Toast.makeText(this,"Saved on this phone • sign in to sync with other phones",Toast.LENGTH_LONG).show();
            render();return;
        }
        saving=true;
        status.setText("Saving "+selected+" to Firebase profile…");
        String uid=account.getUid();
        Map<String,Object> update=new HashMap<>();
        update.put("uid",uid);
        update.put(frames?"equippedFrame":"entranceEffect",selected);
        update.put("updatedAt",FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance().collection("public_profiles").document(uid)
            .set(update,SetOptions.merge())
            .addOnSuccessListener(ok->{
                saving=false;
                FirebaseUser live=FirebaseAuth.getInstance().getCurrentUser();
                if(isFinishing()||isDestroyed()||live==null||!uid.equals(live.getUid()))return;
                applyLocal(frames,selected);
                Toast.makeText(this,"✓ Saved to KING Plus profile",Toast.LENGTH_SHORT).show();
                render();
            })
            .addOnFailureListener(error->{
                saving=false;
                if(isFinishing()||isDestroyed())return;
                status.setText("Firebase save failed. Previous frame is unchanged.");
                Toast.makeText(this,"Sync unavailable • try again when online",Toast.LENGTH_LONG).show();
            });
    }
    private void applyLocal(boolean isFrame,String name){
        if(isFrame)KingCosmetics.setFrame(this,name);
        else KingCosmetics.setEffect(this,name);
    }
}
