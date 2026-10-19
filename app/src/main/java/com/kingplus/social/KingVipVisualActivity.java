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
    private boolean verifiedWallet972;
    private boolean walletUnavailable972;
    private int verifiedLevel972;
    private int selectedTier9930=-1;
    private long verifiedPoints972;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        com.google.firebase.auth.FirebaseUser signed976=
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if(signed976==null){
            verifiedWallet972=false;
            walletUnavailable972=false;
            build();
            return;
        }
        TextView waiting976=tv("Checking verified Diamond Recharge VIP…",17,Color.WHITE,true);
        waiting976.setBackgroundColor(0xff081d2d);
        waiting976.setGravity(Gravity.CENTER);
        setContentView(waiting976);
        KingBackend976.fetchWallet(this,(wallet976,error976)->{
            if(isFinishing()||isDestroyed())return;
            if(wallet976!=null){
                verifiedWallet972=true;
                walletUnavailable972=false;
                verifiedPoints972=wallet976.rechargeTotal;
                verifiedLevel972=wallet976.vipLevel;
            }else{
                verifiedWallet972=false;
                walletUnavailable972=true;
            }
            build();
        });
    }
    private void build(){
        LevelSystem.Snapshot s=LevelSystem.read(this);int vip=verifiedWallet972?verifiedLevel972:0;long shownVipPoints972=verifiedWallet972?verifiedPoints972:0L;int preview9930=selectedTier9930<0?vip:Math.max(0,Math.min(50,selectedTier9930));boolean pink=preview9930>=25;int top=pink?0xff561438:0xff072f58,bottom=pink?0xff18091d:0xff04131f;
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackground(grad(top,bottom,0));
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView back=tv("‹",34,Color.WHITE,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(54)));TextView title=tv("VIP",20,Color.WHITE,true);title.setGravity(Gravity.CENTER);head.addView(title,new LinearLayout.LayoutParams(0,dp(54),1));TextView info=tv("ⓘ",20,Color.WHITE,false);info.setGravity(Gravity.CENTER);info.setOnClickListener(v->new android.app.AlertDialog.Builder(this).setTitle("Verified VIP").setMessage("Your VIP level increases only after verified Diamond Recharge, never from sending FREE Gifts. If verification is unavailable, the screen does not display TEST points as a paid VIP level.").setPositiveButton("OK",null).show());head.addView(info,new LinearLayout.LayoutParams(dp(48),dp(54)));root.addView(head);
        ScrollView sc=new ScrollView(this);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(14),dp(4),dp(14),dp(28));sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        HorizontalScrollView tiers=new HorizontalScrollView(this);tiers.setHorizontalScrollBarEnabled(false);LinearLayout tierRow=new LinearLayout(this);for(int t=0;t<=LevelSystem.MAX_VIP;t++){boolean active=preview9930==t;TextView chip=tv((t>vip?"🔒 ":"✓ ")+"VIP"+t,12,active?0xff17100a:0xffe9e1ef,true);final int selected9930=t;chip.setOnClickListener(v->{selectedTier9930=selected9930;build();});chip.setGravity(Gravity.CENTER);chip.setBackground(grad(active?0xffffd965:0x223b3650,active?0xffffac32:0x221d1929,12));LinearLayout.LayoutParams tlp=new LinearLayout.LayoutParams(dp(70),dp(38));tlp.setMargins(dp(3),0,dp(3),0);tierRow.addView(chip,tlp);}tiers.addView(tierRow);body.addView(tiers,new LinearLayout.LayoutParams(-1,dp(44)));TextView proof9930=tv(verifiedWallet972?"My verified level: VIP"+vip+" • " + shownVipPoints972 + " recharge points":"Verified VIP data unavailable • Tier previews only",12,0xfff9e5ab,true);proof9930.setGravity(Gravity.CENTER);body.addView(proof9930,new LinearLayout.LayoutParams(-1,dp(40)));

        int c1=pink?0xffeb4caa:0xff2d91e8,c2=pink?0xff7c245f:0xff07365e;LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(16),dp(18),dp(16),dp(16));card.setBackground(grad(c1,c2,18));
        TextView badge=tv(preview9930>=40?"👑":preview9930>=28?"♦":preview9930>=14?"♛":"💠",58,Color.WHITE,false);badge.setGravity(Gravity.CENTER);card.addView(badge,new LinearLayout.LayoutParams(-1,dp(76)));
        TextView level=tv("VIP"+preview9930+(preview9930>vip?"  🔒":"  ✓"),28,Color.WHITE,true);level.setGravity(Gravity.CENTER);card.addView(level,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView need=tv(preview9930>vip?"Preview only • "+Math.max(0L,LevelSystem.vipThreshold(preview9930)-shownVipPoints972)+" verified points needed":vip>=LevelSystem.MAX_VIP?"MAX VERIFIED VIP":"Your current verified progress to VIP"+(vip+1),12,0xffeaf7ff,true);need.setGravity(Gravity.CENTER);card.addView(need,new LinearLayout.LayoutParams(-1,dp(34)));
        ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(100);long from=LevelSystem.vipThreshold(vip),to=LevelSystem.vipThreshold(Math.min(LevelSystem.MAX_VIP,vip+1));int pc=vip>=LevelSystem.MAX_VIP?100:(int)Math.max(0,Math.min(100,(shownVipPoints972-from)*100/Math.max(1,to-from)));p.setProgress(pc);LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(18));pp.setMargins(dp(24),dp(4),dp(24),0);card.addView(p,pp);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(230));cp.setMargins(0,dp(6),0,dp(14));body.addView(card,cp);

        TextView pack=tv("VIP"+preview9930+" Benefit Preview"+(preview9930>vip?" • LOCKED":""),14,0xffffe07b,true);body.addView(pack,new LinearLayout.LayoutParams(-1,dp(40)));
        LinearLayout rewards=new LinearLayout(this);String[] ri={"🎁","👑","🖼","✨"};String[] rn={"Gift","Badge","Frame","Entrance"};for(int i=0;i<4;i++){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setGravity(Gravity.CENTER);x.setBackground(grad(0x332a91e8,0x22111122,12));x.addView(tv(ri[i],28,Color.WHITE,false),new LinearLayout.LayoutParams(-1,dp(48)));TextView n=tv(rn[i],10,0xffd6d6e5,true);n.setGravity(Gravity.CENTER);x.addView(n,new LinearLayout.LayoutParams(-1,dp(28)));LinearLayout.LayoutParams xp=new LinearLayout.LayoutParams(0,dp(82),1);xp.setMargins(dp(3),0,dp(3),0);rewards.addView(x,xp);}body.addView(rewards,new LinearLayout.LayoutParams(-1,dp(86)));

        TextView identityShop=tv("👑 VIP ID Number  +  🎨 VIP Name Colors   ›",16,0xffffdf75,true);
        identityShop.setBackground(grad(0xff61470c,0xff352644,12));
        identityShop.setGravity(Gravity.CENTER);
        identityShop.setOnClickListener(v->startActivity(new android.content.Intent(this,KingVipIdentityShopActivity.class)));
        LinearLayout.LayoutParams shopLp=new LinearLayout.LayoutParams(-1,dp(60));shopLp.setMargins(0,dp(14),0,dp(10));
        body.addView(identityShop,shopLp);

        TextView more=tv("See More  ›",12,0xffcbd8ff,true);more.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);more.setOnClickListener(v->new android.app.AlertDialog.Builder(this).setTitle("VIP benefits").setMessage("Paid VIP is paused in free mode. FREE Gifts, Family and Chat have no money transfer or billing. Paid benefits are previews only.").setPositiveButton("OK",null).show());body.addView(more,new LinearLayout.LayoutParams(-1,dp(42)));
        TextView unlock=tv(KingRecharge974Activity.LIVE_RECHARGE?"Recharge verified Diamonds to increase VIP":"VIP Recharge setup pending • payments disabled",13,0xffd6d6e5,true);unlock.setGravity(Gravity.CENTER);body.addView(unlock,new LinearLayout.LayoutParams(-1,dp(52)));
        String[][] perks={{"💎","Exclusive identity"},{"🪽","Entrance effect"},{"🖼","Profile frame"},{"🎙","VIP mic seat"},{"🎁","Gift privilege"},{"🛡","Priority badge"}};
        for(int row=0;row<2;row++){LinearLayout r=new LinearLayout(this);for(int col=0;col<3;col++){int i=row*3+col;LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setGravity(Gravity.CENTER);x.setBackground(grad(0x221c2a40,0x11101822,12));x.addView(tv(perks[i][0],28,Color.WHITE,false),new LinearLayout.LayoutParams(-1,dp(48)));TextView n=tv(perks[i][1],10,0xffd7d8e6,true);n.setGravity(Gravity.CENTER);x.addView(n,new LinearLayout.LayoutParams(-1,dp(30)));LinearLayout.LayoutParams xp=new LinearLayout.LayoutParams(0,dp(88),1);xp.setMargins(dp(4),dp(4),dp(4),dp(4));r.addView(x,xp);}body.addView(r,new LinearLayout.LayoutParams(-1,dp(92)));}
        TextView note=tv(verifiedWallet972?"VIP is based only on verified Diamond Recharge • Gifts do not add VIP":walletUnavailable972?"Verified VIP is unavailable until secure Cloudflare Wallet is configured":"Recharge VIP preview; sign in to view real progress",11,0xff9da6b6,false);note.setGravity(Gravity.CENTER);body.addView(note,new LinearLayout.LayoutParams(-1,dp(46)));
        setContentView(root);
    }
}
