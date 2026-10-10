package com.kingplus.social;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.content.res.ColorStateList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/** The same secure Diamond balance that a future verified Razorpay
 * captured-payment webhook credits to Cloudflare D1. No fake TEST coins,
 * client-side wallet writes, Google billing or purchase checkout are used.
 */
public final class KingWallet976Activity extends Activity {
    private TextView diamonds,vip,status,account;
    private int seq976;
    private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable rounded(int color){
        GradientDrawable d=new GradientDrawable();
        d.setColor(color);d.setCornerRadius(dp(14));return d;
    }
    private TextView text(String label,int size,int color,boolean strong){
        TextView t=new TextView(this);
        t.setText(label);t.setTextSize(size);t.setTextColor(color);
        t.setPadding(dp(14),dp(12),dp(14),dp(12));
        if(strong)t.setTypeface(null,Typeface.BOLD);
        return t;
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        ScrollView scroll=new ScrollView(this);
        LinearLayout layout=new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xff121329);
        layout.setPadding(dp(14),dp(16),dp(14),dp(32));
        scroll.addView(layout);
        setContentView(scroll);
        TextView title=text("‹     💎 KING Plus Diamond Wallet",23,Color.WHITE,true);
        title.setOnClickListener(v->finish());
        layout.addView(title);
        layout.addView(text("Only verified Diamond Recharge counts toward VIP. Sending Gifts spends Diamonds; Gifts never increase VIP.",
            13,0xffc4c5d7,false));

        diamonds=text("💎 Diamonds: not verified yet",22,0xffffe097,true);
        diamonds.setBackground(rounded(0xff2b2550));
        layout.addView(diamonds);
        vip=text("👑 Recharge VIP: not verified yet",17,0xffffe097,true);
        layout.addView(vip);
        account=text("",11,0xffbcbdd0,false);layout.addView(account);
        status=text("Preparing verified wallet…",13,0xffc7c7d6,false);
        layout.addView(status);

        Button refresh=new Button(this);
        refresh.setAllCaps(false);
        refresh.setText("Refresh verified Diamond balance");
        refresh.setTextColor(Color.WHITE);
        refresh.setBackgroundTintList(ColorStateList.valueOf(0xff624bd7));
        LinearLayout.LayoutParams buttonParams=new LinearLayout.LayoutParams(-1,dp(54));
        buttonParams.setMargins(0,dp(8),0,dp(12));
        layout.addView(refresh,buttonParams);
        refresh.setOnClickListener(v->loadWallet());

        TextView packages=text("Diamond Recharge Packs",19,Color.WHITE,true);
        layout.addView(packages);
        String[] products={"💎 100 Diamonds","💎 600 Diamonds","💎 1,300 Diamonds"};
        for(String label:products){
            TextView pack=text(label+"   •   Setup pending",17,0xffe0d6fa,true);
            pack.setBackground(rounded(0xff302c45));
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,dp(70));
            params.bottomMargin=dp(8);layout.addView(pack,params);
            pack.setOnClickListener(v->
                status.setText("Payments are OFF. Verified merchant setup is required before customers can buy Diamonds."));
        }
        TextView details=text("Payment setup is not active. No money will be charged here. Until an approved provider and secure server are configured, the app cannot sell or credit purchased Diamonds.",
            13,0xffffd68e,false);
        details.setBackground(rounded(0xff4a331c));layout.addView(details);
        layout.addView(text("FREE Party Gifts, Emojis and Chat are separate from paid Diamonds. No Diamonds are created by this screen.",
            12,0xffadb1c6,false));
        loadWallet();
    }
    private void loadWallet(){
        final int requestId=++seq976;
        FirebaseUser me=FirebaseAuth.getInstance().getCurrentUser();
        if(me==null){
            account.setText("Not signed in");
            diamonds.setText("💎 Diamonds: sign in first");
            vip.setText("👑 Recharge VIP: sign in first");
            status.setText("Please sign in to read your verified Wallet.");
            return;
        }
        String uid=me.getUid();
        account.setText("Verified Firebase account: "+(uid.length()>12?uid.substring(0,12)+"…":uid));
        diamonds.setText("💎 Diamonds: checking server…");
        vip.setText("👑 Recharge VIP: checking server…");
        status.setText("Connecting to secure Cloudflare Diamond Wallet…");
        KingBackend976.fetchWallet(this,(verified,error)->{
            if(requestId!=seq976)return;
            if(verified==null){
                diamonds.setText("💎 Diamonds: unavailable (not zero)");
                vip.setText("👑 Recharge VIP: unavailable");
                status.setText(error==null?"Cannot verify the Wallet right now.":error);
                return;
            }
            diamonds.setText("💎 Verified Diamonds: "+verified.diamonds);
            vip.setText("👑 VIP "+verified.vipLevel+
                "   •   Recharge total: "+verified.rechargeTotal+" Diamonds");
            status.setText("Verified using your Firebase ID token and the server Wallet.");
        });
    }
}
