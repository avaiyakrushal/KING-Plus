package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.security.SecureRandom;

public final class KingLudoPracticeActivity extends Activity {
    private final SecureRandom rng=new SecureRandom();
    private final int[][] pos={{-1,-1,-1,-1},{-1,-1,-1,-1}};
    private int turn=0,dice=0; private boolean rolled=false,finished=false;
    private TextView status,diceView; private LinearLayout pieces;
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(10),dp(8),dp(10),dp(8));return t;}
    @Override public void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(14),dp(14),dp(20));root.setBackgroundColor(0xff10264a);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView back=tv("‹",34,Color.WHITE,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(52)));TextView title=tv("KING LUDO • OFFLINE",20,Color.WHITE,true);title.setGravity(Gravity.CENTER);head.addView(title,new LinearLayout.LayoutParams(0,dp(52),1));root.addView(head);
        status=tv("",15,0xffffdd68,true);status.setGravity(Gravity.CENTER);status.setBackground(bg(0xff203b68,14));root.addView(status,new LinearLayout.LayoutParams(-1,dp(62)));
        diceView=tv("🎲  —",32,Color.WHITE,true);diceView.setGravity(Gravity.CENTER);root.addView(diceView,new LinearLayout.LayoutParams(-1,dp(88)));
        pieces=new LinearLayout(this);pieces.setOrientation(LinearLayout.VERTICAL);root.addView(pieces,new LinearLayout.LayoutParams(-1,0,1));
        Button roll=new Button(this);roll.setText("ROLL DICE");roll.setAllCaps(false);roll.setTextSize(18);roll.setTextColor(Color.WHITE);roll.setBackground(bg(0xff7b4bd4,16));roll.setOnClickListener(v->roll());root.addView(roll,new LinearLayout.LayoutParams(-1,dp(58)));
        TextView rules=tv("2-player hot-seat practice • roll 6 to leave yard • first to bring all four pieces home wins",11,0xffc6d7ef,false);rules.setGravity(Gravity.CENTER);root.addView(rules,new LinearLayout.LayoutParams(-1,dp(52)));
        setContentView(root);render();
    }
    private void roll(){if(finished)return;if(rolled){toast("Move a piece first");return;}dice=1+rng.nextInt(6);rolled=true;diceView.setText("🎲  "+dice);if(!hasMove(turn,dice)){toast("No valid move");nextTurn();}render();}
    private boolean hasMove(int p,int d){for(int k=0;k<4;k++){int x=pos[p][k];if(x<0?d==6:(x<56&&x+d<=56))return true;}return false;}
    private void move(int k){if(finished||!rolled)return;int x=pos[turn][k];if(!(x<0?dice==6:(x<56&&x+dice<=56))){toast("That piece cannot move");return;}int oldTurn=turn;int target=x<0?0:x+dice;pos[turn][k]=target;boolean capture=false;if(target>=0&&target<52&&target%13!=0){int other=1-turn;for(int j=0;j<4;j++){if(pos[other][j]>=0&&pos[other][j]<52&&shared(other,pos[other][j])==shared(turn,target)){pos[other][j]=-1;capture=true;}}}boolean win=true;for(int j=0;j<4;j++)if(pos[turn][j]!=56)win=false;if(win){finished=true;status.setText("🏆 PLAYER "+(turn+1)+" WINS");new AlertDialog.Builder(this).setTitle("Offline Ludo").setMessage("Player "+(turn+1)+" wins!").setPositiveButton("Play again",(d,w)->restart()).setNegativeButton("Close",(d,w)->finish()).show();render();return;}boolean bonus=dice==6||capture||target==56;rolled=false;dice=0;if(!bonus)turn=1-turn;diceView.setText("🎲  —");if(oldTurn==turn&&bonus)toast("Bonus turn");render();}
    private int shared(int player,int x){int seat=player==0?0:2;return (seat*13+x)%52;}
    private void nextTurn(){rolled=false;dice=0;turn=1-turn;diceView.setText("🎲  —");render();}
    private void restart(){for(int p=0;p<2;p++)for(int k=0;k<4;k++)pos[p][k]=-1;turn=0;dice=0;rolled=false;finished=false;diceView.setText("🎲  —");render();}
    private void render(){status.setText(finished?status.getText():"PLAYER "+(turn+1)+(rolled?" • move a piece":" • roll dice"));pieces.removeAllViews();for(int p=0;p<2;p++){TextView name=tv((p==0?"🔴":"🟡")+" Player "+(p+1),15,Color.WHITE,true);pieces.addView(name,new LinearLayout.LayoutParams(-1,dp(44)));LinearLayout row=new LinearLayout(this);for(int k=0;k<4;k++){final int piece=k;Button b=new Button(this);b.setAllCaps(false);b.setText(label(k,pos[p][k]));b.setTextSize(10);b.setEnabled(!finished&&rolled&&p==turn&&(pos[p][k]<0?dice==6:(pos[p][k]<56&&pos[p][k]+dice<=56)));b.setOnClickListener(v->move(piece));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(58),1);lp.setMargins(dp(2),dp(2),dp(2),dp(2));row.addView(b,lp);}pieces.addView(row,new LinearLayout.LayoutParams(-1,dp(64)));}}
    private String label(int k,int x){return "P"+(k+1)+"\n"+(x<0?"Yard":x>=56?"HOME":String.valueOf(x));}
    private void toast(String s){android.widget.Toast.makeText(this,s,android.widget.Toast.LENGTH_SHORT).show();}
}
