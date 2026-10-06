package com.kingplus.social;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class KingVipVisualActivity extends Activity {
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable grad(int a,int b,int r){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});g.setCornerRadius(dp(r));return g;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(null,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(10),dp(6),dp(10),dp(6));return t;}
    @Override public void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){
        LevelSystem.Snapshot s=LevelSystem.read(this);int vip=s.vipLevel;boolean pink=vip>=25;int top=pink?0xff561438:0xff072f58,bottom=pink?0xff18091d:0xff04131f;
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackground(grad(top,bottom,0));
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView back=tv("‹",34,Color.WHITE,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(54)));TextView title=tv("VIP",20,Color.WHITE,true);title.setGravity(Gravity.CENTER);head.addView(title,new LinearLayout.LayoutParams(0,dp(54),1));TextView info=tv("ⓘ",20,Color.WHITE,false);info.setGravity(Gravity.CENTER);head.addView(info,new LinearLayout.LayoutParams(dp(48),dp(54)));root.addView(head);
        ScrollView sc=new ScrollView(this);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(14),dp(4),dp(14),dp(28));sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));

        int c1=pink?0xffeb4caa:0xff2d91e8,c2=pink?0xff7c245f:0xff07365e;LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(16),dp(18),dp(16),dp(16));card.setBackground(grad(c1,c2,18));
        TextView badge=tv("💠",58,Color.WHITE,false);badge.setGravity(Gravity.CENTER);card.addView(badge,new LinearLayout.LayoutParams(-1,dp(76)));
        TextView level=tv("VIP"+vip,28,Color.WHITE,true);level.setGravity(Gravity.CENTER);card.addView(level,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView need=tv(vip>=LevelSystem.MAX_VIP?"MAX VIP":"Progress to VIP"+(vip+1),12,0xffeaf7ff,true);need.setGravity(Gravity.CENTER);card.addView(need,new LinearLayout.LayoutParams(-1,dp(34)));
        ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(100);long from=LevelSystem.vipThreshold(vip),to=s.nextVipPoints();int pc=vip>=LevelSystem.MAX_VIP?100:(int)Math.max(0,Math.min(100,(s.vipPoints-from)*100/Math.max(1,to-from)));p.setProgress(pc);LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(18));pp.setMargins(dp(24),dp(4),dp(24),0);card.addView(p,pp);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(230));cp.setMargins(0,dp(6),0,dp(14));body.addView(card,cp);

        TextView pack=tv("First VIP Reward Pack",14,0xffffe07b,true);body.addView(pack,new LinearLayout.LayoutParams(-1,dp(40)));
        LinearLayout rewards=new LinearLayout(this);String[] ri={"🎁","👑","🖼","✨"};String[] rn={"Gift","Badge","Frame","Entrance"};for(int i=0;i<4;i++){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setGravity(Gravity.CENTER);x.setBackground(grad(0x332a91e8,0x22111122,12));x.addView(tv(ri[i],28,Color.WHITE,false),new LinearLayout.LayoutParams(-1,dp(48)));TextView n=tv(rn[i],10,0xffd6d6e5,true);n.setGravity(Gravity.CENTER);x.addView(n,new LinearLayout.LayoutParams(-1,dp(28)));LinearLayout.LayoutParams xp=new LinearLayout.LayoutParams(0,dp(82),1);xp.setMargins(dp(3),0,dp(3),0);rewards.addView(x,xp);}body.addView(rewards,new LinearLayout.LayoutParams(-1,dp(86)));

        TextView more=tv("See More  ›",12,0xffcbd8ff,true);more.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);body.addView(more,new LinearLayout.LayoutParams(-1,dp(42)));
        TextView unlock=tv("Upgrade VIP to unlock more awesome rewards",13,0xffd6d6e5,true);unlock.setGravity(Gravity.CENTER);body.addView(unlock,new LinearLayout.LayoutParams(-1,dp(52)));
        String[][] perks={{"💎","Exclusive identity"},{"🪽","Entrance effect"},{"🖼","Profile frame"},{"🎙","VIP mic seat"},{"🎁","Gift privilege"},{"🛡","Priority badge"}};
        for(int row=0;row<2;row++){LinearLayout r=new LinearLayout(this);for(int col=0;col<3;col++){int i=row*3+col;LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setGravity(Gravity.CENTER);x.setBackground(grad(0x221c2a40,0x11101822,12));x.addView(tv(perks[i][0],28,Color.WHITE,false),new LinearLayout.LayoutParams(-1,dp(48)));TextView n=tv(perks[i][1],10,0xffd7d8e6,true);n.setGravity(Gravity.CENTER);x.addView(n,new LinearLayout.LayoutParams(-1,dp(30)));LinearLayout.LayoutParams xp=new LinearLayout.LayoutParams(0,dp(88),1);xp.setMargins(dp(4),dp(4),dp(4),dp(4));r.addView(x,xp);}body.addView(r,new LinearLayout.LayoutParams(-1,dp(92)));}
        TextView note=tv("No billing enabled • VIP progression uses KING Plus activity/test points",11,0xff9da6b6,false);note.setGravity(Gravity.CENTER);body.addView(note,new LinearLayout.LayoutParams(-1,dp(46)));
        setContentView(root);
    }
}
