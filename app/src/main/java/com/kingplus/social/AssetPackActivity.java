package com.kingplus.social;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AssetPackActivity extends Activity {
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView t(String s,int z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(0xff1f1b25);if(b)v.setTypeface(null,Typeface.BOLD);v.setPadding(dp(12),dp(10),dp(12),dp(10));return v;}
    @Override public void onCreate(Bundle b){super.onCreate(b);ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(16),dp(20),dp(16),dp(30));root.setBackgroundColor(0xfff7f5fb);TextView back=t("‹  KING Plus Asset Pack",24,true);back.setOnClickListener(v->finish());root.addView(back);root.addView(t("Original bundled visuals and sounds used by KING Plus. No copied Bolo Hi assets.",13,false));add(root,"🎁 Gift Collection","🌹 Rose   💗 Heart   👑 Crown   💎 Diamond\n🎆 Firework   🦁 Lion   🌟 Star   🪽 Wings\n🏰 Castle   🚀 Rocket   🐉 Dragon   🎉 Party");add(root,"😊 Sticker Packs","Faces • Festival • Laugh • World • Yellow • Cute • VIP • Lion\nLive expressions • favourites • room reactions");add(root,"🎨 Room Themes","Royal Purple • Neon Night • Emerald Party • Ocean Blue • Sunset Gold • Festival • Gaming • VIP Lounge");add(root,"🏅 VIP / Level Frames","Normal Lv.1–99 • VIP 0–12 • badges • medals • entrance labels");add(root,"🔊 Sound FX","Gift send • room join • level up • game win");add(root,"🎮 Game Pack","Ludo Race • Tic Tac Toe • Slots • Coin Toss • RPS • Dice • Guess • Memory Match • Reaction Tap • High / Low • Lucky Wheel");sc.addView(root);setContentView(sc);}
    private void add(LinearLayout root,String title,String body){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(8),dp(6),dp(8),dp(8));c.setBackground(bg(Color.WHITE,18));c.addView(t(title,17,true));c.addView(t(body,14,false));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(8),0,dp(8));root.addView(c,p);}
}
