package com.kingplus.social;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public final class KingWalletVisualActivity extends Activity {
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(12),dp(7),dp(12),dp(7));return t;}
    @Override public void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){
        SharedPreferences p=getSharedPreferences("MainActivity",MODE_PRIVATE);String name=p.getString("name","KING User");int coins=p.getInt("coins",0),gifts=p.getInt("gift_count",0);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xfffbfafc);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView back=tv("‹",34,0xff222222,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(52),dp(56)));TextView title=tv("My Wallet",20,0xff181818,true);title.setGravity(Gravity.CENTER);head.addView(title,new LinearLayout.LayoutParams(0,dp(56),1));head.addView(tv("⋮",24,0xff444444,false),new LinearLayout.LayoutParams(dp(52),dp(56)));root.addView(head);
        ScrollView sc=new ScrollView(this);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(16),dp(4),dp(16),dp(26));sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout user=new LinearLayout(this);user.setGravity(Gravity.CENTER_VERTICAL);TextView av=tv("👑",24,0xff6e5400,false);av.setGravity(Gravity.CENTER);av.setBackground(bg(0xfffff0bf,28));user.addView(av,new LinearLayout.LayoutParams(dp(52),dp(52)));TextView nm=tv(name,15,0xff202020,true);user.addView(nm,new LinearLayout.LayoutParams(0,dp(52),1));TextView tag=tv("TEST",10,0xff8b6a00,true);tag.setGravity(Gravity.CENTER);tag.setBackground(bg(0xffffef9a,12));user.addView(tag,new LinearLayout.LayoutParams(dp(58),dp(30)));body.addView(user,new LinearLayout.LayoutParams(-1,dp(66)));
        LinearLayout tabs=new LinearLayout(this);TextView diamond=tv("Diamond",14,0xff181818,true);diamond.setGravity(Gravity.CENTER);diamond.setBackground(bg(0xffffe600,10));tabs.addView(diamond,new LinearLayout.LayoutParams(0,dp(44),1));TextView crystal=tv("Crystal",14,0xff777777,true);crystal.setGravity(Gravity.CENTER);crystal.setBackground(bg(0xfff0eff3,10));LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,dp(44),1);tp.setMargins(dp(8),0,0,0);tabs.addView(crystal,tp);body.addView(tabs,new LinearLayout.LayoutParams(-1,dp(48)));
        LinearLayout bal=new LinearLayout(this);bal.setOrientation(LinearLayout.VERTICAL);bal.setGravity(Gravity.CENTER);bal.setBackground(bg(0xfffff4d2,16));TextView amt=tv("💎  "+coins,30,0xff342700,true);amt.setGravity(Gravity.CENTER);bal.addView(amt,new LinearLayout.LayoutParams(-1,dp(58)));TextView desc=tv("KING test balance • no cash value",12,0xff8a7040,false);desc.setGravity(Gravity.CENTER);bal.addView(desc,new LinearLayout.LayoutParams(-1,dp(32)));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(112));bp.setMargins(0,dp(10),0,dp(12));body.addView(bal,bp);
        LinearLayout notice=new LinearLayout(this);notice.setOrientation(LinearLayout.VERTICAL);notice.setPadding(dp(14),dp(12),dp(14),dp(12));notice.setBackground(bg(0xffefe5ff,14));notice.addView(tv("👑 KING Plus Wallet",15,0xff5d2f94,true));notice.addView(tv("No billing is enabled. Diamonds here are TEST credits used only for gifts, rooms and feature testing.",12,0xff6c5c79,false));body.addView(notice,new LinearLayout.LayoutParams(-1,-2));
        section(body,"Wallet tools");
        row(body,"🎁","Gift Inventory",gifts+" test items / gifts");
        row(body,"🧾","Transaction History","Local and cloud test activity");
        row(body,"🏆","Contribution & Income","Room gift contribution stats");
        row(body,"🔐","Wallet Safety","Server-authoritative wallet checks");
        section(body,"Billing");
        TextView disabled=tv("Real recharge / Google Play billing is disabled in this KING Plus build.",13,0xff8a7d8e,true);disabled.setGravity(Gravity.CENTER);disabled.setBackground(bg(0xfff1f0f3,12));body.addView(disabled,new LinearLayout.LayoutParams(-1,dp(64)));
        setContentView(root);
    }
    private void section(LinearLayout b,String s){TextView t=tv(s,13,0xff5d5763,true);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(42));p.setMargins(0,dp(10),0,0);b.addView(t,p);}
    private void row(LinearLayout b,String i,String a,String sub){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(10),dp(6),dp(10),dp(6));r.setBackground(bg(Color.WHITE,14));TextView ic=tv(i,23,0xff222222,false);ic.setGravity(Gravity.CENTER);r.addView(ic,new LinearLayout.LayoutParams(dp(48),dp(56)));LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.addView(tv(a,14,0xff222222,true),new LinearLayout.LayoutParams(-1,dp(30)));info.addView(tv(sub,11,0xff8b858e,false),new LinearLayout.LayoutParams(-1,dp(24)));r.addView(info,new LinearLayout.LayoutParams(0,dp(56),1));TextView ar=tv("›",24,0xffaaa5ad,false);ar.setGravity(Gravity.CENTER);r.addView(ar,new LinearLayout.LayoutParams(dp(32),dp(56)));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(68));p.setMargins(0,dp(4),0,dp(4));b.addView(r,p);}
}
