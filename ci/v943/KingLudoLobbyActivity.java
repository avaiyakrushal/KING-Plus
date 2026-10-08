package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class KingLudoLobbyActivity extends Activity {
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setGravity(Gravity.CENTER);if(b)t.setTypeface(null,Typeface.BOLD);return t;}
    @Override public void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){
        int coins=Math.max(0,getSharedPreferences("MainActivity",MODE_PRIVATE).getInt("coins",0));
        FrameLayout root=new FrameLayout(this);GradientDrawable gd=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xff0b4b8c,0xff0a76c4,0xff083c78});root.setBackground(gd);
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(14),dp(14),dp(14),dp(18));root.addView(page,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=tv("‹",34,Color.WHITE,false);back.setOnClickListener(v->finish());top.addView(back,new LinearLayout.LayoutParams(dp(46),dp(50)));
        TextView avatar=tv("👑",20,Color.WHITE,true);avatar.setBackground(bg(0x33000000,22));top.addView(avatar,new LinearLayout.LayoutParams(dp(44),dp(44)));
        TextView title=tv("KING LUDO",14,Color.WHITE,true);title.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);title.setPadding(dp(10),0,0,0);top.addView(title,new LinearLayout.LayoutParams(0,dp(50),1));
        TextView coin=tv("🪙 "+coins,13,Color.WHITE,true);coin.setBackground(bg(0x33000000,18));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(84),dp(38));cp.setMargins(dp(4),0,dp(4),0);top.addView(coin,cp);
        TextView gem=tv("💎 "+coins,13,Color.WHITE,true);gem.setBackground(bg(0x33000000,18));top.addView(gem,new LinearLayout.LayoutParams(dp(84),dp(38)));page.addView(top,new LinearLayout.LayoutParams(-1,dp(56)));

        LudoArt art=new LudoArt(this);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,0,1);ap.setMargins(dp(10),dp(4),dp(10),dp(12));page.addView(art,ap);

        LinearLayout row1=new LinearLayout(this);row1.setGravity(Gravity.CENTER);row1.addView(mode("⚡","QUICK ONLINE",()->openOnline(2)),lp());row1.addView(mode("🌐","ONLINE 4P",()->openOnline(4)),lp());page.addView(row1,new LinearLayout.LayoutParams(-1,dp(82)));
        LinearLayout row2=new LinearLayout(this);row2.setGravity(Gravity.CENTER);row2.addView(mode("🎯","ROOM CODE",this::openJoin),lp());row2.addView(mode("💬","CHAT ROOM",this::openParty),lp());page.addView(row2,new LinearLayout.LayoutParams(-1,dp(82)));
        LinearLayout row3=new LinearLayout(this);row3.setGravity(Gravity.CENTER);row3.addView(mode("🏆","CHAMPIONSHIP",this::openChampionship),lp());row3.addView(mode("🎲","OFFLINE",()->startActivity(new Intent(this,KingLudoPracticeActivity.class))),lp());page.addView(row3,new LinearLayout.LayoutParams(-1,dp(82)));

        LinearLayout bottom=new LinearLayout(this);bottom.setGravity(Gravity.CENTER);
        addBottom(bottom,"🛍","SHOP",()->startActivity(new Intent(this,KingWardrobeActivity.class)));
        addBottom(bottom,"🏅","RANK",()->startActivity(new Intent(this,DiscoverActivity.class)));
        addBottom(bottom,"▶","PLAY",()->openOnline(4));
        addBottom(bottom,"🎪","EVENT",this::openEvents);
        addBottom(bottom,"🎁","REWARD",()->startActivity(new Intent(this,KingVipVisualActivity.class)));
        page.addView(bottom,new LinearLayout.LayoutParams(-1,dp(58)));
        setContentView(root);
    }
    private LinearLayout.LayoutParams lp(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(72),1);p.setMargins(dp(5),dp(5),dp(5),dp(5));return p;}
    private View mode(String icon,String text,Runnable action){LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.setGravity(Gravity.CENTER);b.setBackground(bg(0xff0f8bd5,12));TextView i=tv(icon,24,Color.WHITE,false);TextView t=tv(text,11,Color.WHITE,true);b.addView(i,new LinearLayout.LayoutParams(-1,dp(38)));b.addView(t,new LinearLayout.LayoutParams(-1,dp(28)));b.setOnClickListener(v->KingSafe.run(this,"ludo:"+text,action));return b;}
    private void addBottom(LinearLayout bottom,String icon,String label,Runnable action){LinearLayout btm=new LinearLayout(this);btm.setOrientation(LinearLayout.VERTICAL);btm.setGravity(Gravity.CENTER);TextView ic=tv(icon,19,Color.WHITE,false);TextView lb=tv(label,9,0xffd8ecff,true);btm.addView(ic,new LinearLayout.LayoutParams(-1,dp(30)));btm.addView(lb,new LinearLayout.LayoutParams(-1,dp(20)));btm.setOnClickListener(v->KingSafe.run(this,"ludo-bottom:"+label,action));bottom.addView(btm,new LinearLayout.LayoutParams(0,dp(56),1));}
    private void openOnline(int players){Intent i=new Intent(this,OnlineLudoActivity.class);i.putExtra("maxPlayers",players);startActivity(i);}
    private void openJoin(){Intent i=new Intent(this,OnlineLudoActivity.class);startActivity(i);}
    private void openParty(){Intent i=new Intent(this,PartyActivity.class);i.putExtra("requestedGame","ludo");startActivity(i);}
    private void openChampionship(){new AlertDialog.Builder(this).setTitle("KING Ludo Championship").setMessage("Championship rounds use the realtime 4-player Ludo room. Create a room, share its code, and the winner is recorded in the match result.").setPositiveButton("Start 4-player round",(d,w)->openOnline(4)).setNegativeButton("Close",null).show();}
    private void openEvents(){Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab","Events");startActivity(i);}
    private static final class LudoArt extends View {
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);public LudoArt(android.content.Context c){super(c);}
        protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight(),cx=w/2f,cy=h/2f;Paint q=p;q.setColor(0x552ad7ff);c.drawCircle(cx,cy,Math.min(w,h)*.45f,q);q.setColor(Color.WHITE);q.setTextAlign(Paint.Align.CENTER);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(Math.min(w,h)*.16f);c.drawText("LUDO",cx,cy-10,q);q.setTextSize(Math.min(w,h)*.13f);q.setColor(0xffffd73a);c.drawText("LORD",cx,cy+Math.min(w,h)*.13f,q);float s=Math.min(w,h)*.11f;int[] colors={0xffff4f55,0xff36c76d,0xffffd44a,0xff4aa3ff};float[][] pos={{cx-s*2.5f,cy-s*2.2f},{cx+s*2.5f,cy-s*2.2f},{cx-s*2.5f,cy+s*2.4f},{cx+s*2.5f,cy+s*2.4f}};for(int i=0;i<4;i++){q.setColor(colors[i]);c.drawCircle(pos[i][0],pos[i][1],s,q);q.setColor(Color.WHITE);c.drawCircle(pos[i][0],pos[i][1],s*.36f,q);}}
    }
}
