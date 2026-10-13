package com.kingplus.social;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.content.Intent;
import android.view.Gravity;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Account Me Noble/VIP: self read from JWT-authenticated Wallet Worker only.
 * Separates mathematically previewed tiers from actual backend entitlements.
 * NO spend, no VIP/Noble grants, no paid entitlement from local activity.
 */
public final class KingMeVerifiedPrestige9988Activity extends Activity {
 private LinearLayout body;
 private FirebaseUser me;
 private int verifiedVip=0;
 private long rechargePoints=0;
 private boolean walletReady=false;
 private String message="Checking verified Wallet…";
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private TextView text(String s,int z,int color){
  TextView v=new TextView(this);v.setText(s);v.setTextColor(color);
  v.setTextSize(z);v.setPadding(dp(10),dp(9),dp(10),dp(9));return v;
 }
 private void button(String name,Runnable action){
  Button btn=new Button(this);btn.setAllCaps(false);btn.setText(name);
  body.addView(btn,new LinearLayout.LayoutParams(-1,dp(55)));
  btn.setOnClickListener(v->action.run());
 }
 @Override public void onCreate(Bundle b){
  super.onCreate(b);me=FirebaseAuth.getInstance().getCurrentUser();
  render();refresh();
 }
 private void refresh(){
  final FirebaseUser now=FirebaseAuth.getInstance().getCurrentUser();
  if(now==null){walletReady=false;message="Sign in to see actual verified VIP.";render();return;}
  final String owner=now.getUid();
  walletReady=false;message="Checking Wallet Server using your Firebase login…";render();
  KingBackend976.fetchWallet(this,(wallet,error)->{
   FirebaseUser current=FirebaseAuth.getInstance().getCurrentUser();
   if(current==null||!owner.equals(current.getUid())||isFinishing()||isDestroyed())return;
   if(wallet!=null&&KingPrestigeIdentity9988.validVip(wallet.vipLevel)
      &&KingPrestigeIdentity9988.validRecharge(wallet.rechargeTotal)){
     walletReady=true;verifiedVip=wallet.vipLevel;
     rechargePoints=wallet.rechargeTotal;message="Wallet verified by secure server";
   }else{
     walletReady=false;verifiedVip=0;rechargePoints=0;
     message=error==null?"Verified VIP is unavailable.":error;
   }
   render();
  });
 }
 private void render(){
  ScrollView sc=new ScrollView(this);sc.setBackgroundColor(0xff1d1731);
  body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
  body.setPadding(dp(14),dp(14),dp(14),dp(34));sc.addView(body);setContentView(sc);
  TextView title=text("‹   KING Plus • Verified Prestige",23,0xffffdc80);
  title.setOnClickListener(v->finish());body.addView(title);
  body.addView(text(message,14,0xffe0d7eb));
  String vip=KingPrestigeIdentity9988.vipLabel(walletReady,verifiedVip);
  body.addView(text("💎  "+vip,20,Color.WHITE));
  if(walletReady)body.addView(text("Verified recharge total: "+rechargePoints+" points",14,0xffe5d8f2));
  else body.addView(text("Your phone-local gifts and XP cannot be counted as paid VIP.",13,0xffffbb94));
  int preview=walletReady?KingPrestigeIdentity9988.noblePreviewForVip(verifiedVip):0;
  body.addView(text("♛  Noble tiers — PREVIEW ONLY",19,0xffffdc80));
  body.addView(text("VIP-based suggested Noble tier: "+preview+"/10"+
   "\nThis is NOT an active Noble benefit. A trusted Noble grant and production entitlement service are not configured.",
    14,Color.WHITE));
  for(int n=1;n<=10;n++){
   int target=n*5;
   body.addView(text("◇ Noble "+n+"  •  Tier preview at VIP "+target+
      (walletReady&&verifiedVip>=target?"  •  eligibility only":"  •  not earned"),
      13,0xffe2d9ee));
  }
  body.addView(text("🎭 Cosmetics and room privileges",18,0xffffdc80));
  body.addView(text("Frames, entrances, VIP seats, paid gift unlocks and Noble special effects require a server-side verified entitlement. TEST level or Gift points do not activate them.",
    13,Color.WHITE));
  button("↻ Refresh verified Wallet",this::refresh);
  button("💎 Open original verified VIP tiers",()->startActivity(new Intent(this,KingVipVisualActivity.class)));
  button("💳 Verified Wallet Status",()->startActivity(new Intent(this,KingWallet976Activity.class)));
  button("⭐ Activity Level (TEST only)",()->{
   new android.app.AlertDialog.Builder(this).setTitle("Activity XP")
      .setMessage("Local XP/Level reflects this phone's test activity and is NOT paid VIP or Noble. A production level service still needs server-side proof.")
      .setPositiveButton("OK",null).show();
  });
 }
}
