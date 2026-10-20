package com.kingplus.social;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.security.SecureRandom;

public class KingOfflineLudoActivity extends Activity {
    private static final int BG=0xff171126, CARD=0xff2a203d, GOLD=0xffffd768, MUTED=0xffc8bed7;
    private final SecureRandom random=new SecureRandom();
    private final int[][] pieces={{-1,-1,-1,-1},{-1,-1,-1,-1}};
    private int current=0,dice=0,sixes=0;
    private boolean rolled=false,finished=false;
    private TextView status,turn,diceView;
    private LinearLayout piecesBox,actions;
    private Board board;

    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(10),dp(7),dp(10),dp(7));return t;}
    private Button btn(String s){Button b=new Button(this);b.setAllCaps(false);b.setText(s);b.setTextColor(Color.WHITE);b.setTextSize(14);b.setBackground(bg(0xff8754df,14));return b;}

    @Override public void onCreate(Bundle b){super.onCreate(b);build();render("Offline game ready • Red starts");}

    private void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        ScrollView sc=new ScrollView(this);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(14),dp(8),dp(14),dp(28));sc.addView(page);root.addView(sc,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView back=tv("‹",34,Color.WHITE,true);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(54)));LinearLayout titles=new LinearLayout(this);titles.setOrientation(LinearLayout.VERTICAL);titles.addView(tv("🎲 KING Offline Ludo",22,Color.WHITE,true));titles.addView(tv("2 players • pass-and-play • no internet needed",12,MUTED,false));head.addView(titles,new LinearLayout.LayoutParams(0,dp(64),1));page.addView(head);
        status=tv("",13,GOLD,true);status.setGravity(Gravity.CENTER);status.setBackground(bg(CARD,14));page.addView(status,new LinearLayout.LayoutParams(-1,dp(48)));
        turn=tv("",17,Color.WHITE,true);turn.setGravity(Gravity.CENTER);page.addView(turn,new LinearLayout.LayoutParams(-1,dp(44)));
        board=new Board();LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(330));bp.setMargins(0,dp(4),0,dp(8));page.addView(board,bp);
        diceView=tv("🎲 —",32,GOLD,true);diceView.setGravity(Gravity.CENTER);diceView.setBackground(bg(CARD,18));page.addView(diceView,new LinearLayout.LayoutParams(-1,dp(72)));
        piecesBox=new LinearLayout(this);piecesBox.setOrientation(LinearLayout.VERTICAL);page.addView(piecesBox,new LinearLayout.LayoutParams(-1,-2));
        actions=new LinearLayout(this);actions.setOrientation(LinearLayout.VERTICAL);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,-2);ap.setMargins(0,dp(8),0,0);page.addView(actions,ap);
        setContentView(root);ViewCompat.setOnApplyWindowInsetsListener(root,(v,in)->{Insets z=in.getInsets(WindowInsetsCompat.Type.systemBars());v.setPadding(z.left,z.top,z.right,z.bottom);return in;});ViewCompat.requestApplyInsets(root);
    }

    private String player(){return current==0?"🔴 RED":"🟡 YELLOW";}
    private int startOffset(int p){return p==0?0:26;}
    private boolean safeSquare(int global){int m=((global%52)+52)%52;return m%13==0||m%13==8;}
    private boolean canMove(int p,int k){if(finished||!rolled||p!=current)return false;int pos=pieces[p][k];return pos<0?dice==6:(pos<56&&pos+dice<=56);}
    private boolean anyMove(){for(int k=0;k<4;k++)if(canMove(current,k))return true;return false;}
    private boolean allHome(int p){for(int x:pieces[p])if(x!=56)return false;return true;}

    private void roll(){
        if(finished||rolled)return;
        dice=random.nextInt(6)+1;sixes=dice==6?sixes+1:0;
        if(sixes>=3){diceView.setText("🎲 6 • three sixes");status.setText("Three consecutive sixes • turn passes");sixes=0;switchTurn();render(null);return;}
        rolled=true;
        if(!anyMove()){String msg=dice==6?"No legal move":"No legal move • turn passes";if(dice!=6)switchTurn();rolled=false;render(msg);return;}
        render("Dice "+dice+" • choose a highlighted piece");
    }

    private void move(int k){
        if(!canMove(current,k))return;
        int before=pieces[current][k];int target=before<0?0:before+dice;pieces[current][k]=target;
        boolean captured=false,home=target==56;
        if(target>=0&&target<=50){
            int g=(startOffset(current)+target)%52;
            if(!safeSquare(g)){
                int other=1-current;
                for(int q=0;q<4;q++){int op=pieces[other][q];if(op>=0&&op<=50&&((startOffset(other)+op)%52)==g){pieces[other][q]=-1;captured=true;}}
            }
        }
        if(allHome(current)){finished=true;rolled=false;render("🏆 "+player()+" wins the offline match");return;}
        boolean bonus=dice==6||captured||home;
        rolled=false;dice=0;if(!bonus){sixes=0;switchTurn();}
        render(captured?"Captured a piece • bonus turn":home?"Piece reached HOME • bonus turn":bonus?"Six • roll again":"Move complete");
    }

    private void switchTurn(){current=1-current;dice=0;rolled=false;sixes=0;}
    private String pieceLabel(int p,int k){int x=pieces[p][k];if(x<0)return "P"+(k+1)+" • Yard";if(x>=56)return "P"+(k+1)+" • HOME";return "P"+(k+1)+" • "+x+"/56";}

    private void render(String message){
        if(message!=null)status.setText(message);
        turn.setText(finished?"Match finished":player()+" TURN");
        diceView.setText(dice>0?"🎲 "+dice:"🎲 —");
        piecesBox.removeAllViews();
        int[] colors={0xffff5868,0xffffca42};
        for(int p=0;p<2;p++){
            TextView label=tv((p==0?"🔴 RED":"🟡 YELLOW")+(p==current&&!finished?"  • active":""),14,Color.WHITE,true);piecesBox.addView(label,new LinearLayout.LayoutParams(-1,dp(38)));
            LinearLayout row=new LinearLayout(this);
            for(int k=0;k<4;k++){Button b=btn(pieceLabel(p,k));b.setTextSize(11);b.setBackground(bg(colors[p],12));b.setEnabled(canMove(p,k));b.setAlpha(b.isEnabled()?1f:.62f);final int fp=p,fk=k;b.setOnClickListener(v->{if(fp==current)move(fk);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(48),1);lp.setMargins(dp(2),0,dp(2),0);row.addView(b,lp);}
            piecesBox.addView(row,new LinearLayout.LayoutParams(-1,dp(52)));
        }
        actions.removeAllViews();
        if(!finished){Button roll=btn(rolled?"Choose a piece":"🎲 ROLL DICE");roll.setTextSize(18);roll.setEnabled(!rolled);roll.setOnClickListener(v->roll());actions.addView(roll,new LinearLayout.LayoutParams(-1,dp(58)));}
        Button reset=btn(finished?"↻ Play Again":"↻ Restart Match");reset.setOnClickListener(v->reset());LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(50));rp.setMargins(0,dp(7),0,0);actions.addView(reset,rp);
        board.invalidate();
    }

    private void reset(){for(int p=0;p<2;p++)for(int k=0;k<4;k++)pieces[p][k]=-1;current=0;dice=0;sixes=0;rolled=false;finished=false;render("New offline match • Red starts");}

    private final class Board extends View {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        Board(){super(KingOfflineLudoActivity.this);setBackground(bg(0xfff4f1f8,18));}
        @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight(),cx=w/2f,cy=h/2f,r=Math.min(w,h)*.36f;
            p.setStyle(Paint.Style.FILL);p.setColor(0xffeadff6);c.drawCircle(cx,cy,r*1.12f,p);
            for(int i=0;i<52;i++){double a=-Math.PI/2+2*Math.PI*i/52;float x=cx+(float)Math.cos(a)*r,y=cy+(float)Math.sin(a)*r;p.setColor(safeSquare(i)?0xffffdf72:Color.WHITE);c.drawCircle(x,y,dp(4.2f),p);}
            drawHome(c,cx-r*.72f,cy,0xffff5868,"RED");drawHome(c,cx+r*.72f,cy,0xffffca42,"YELLOW");
            for(int pl=0;pl<2;pl++)for(int k=0;k<4;k++)drawPiece(c,pl,k,cx,cy,r);
            p.setColor(0xff4f3e68);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(16));c.drawText("KING LUDO",cx,cy+dp(5),p);
        }
        private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
        private void drawHome(Canvas c,float x,float y,int color,String label){p.setColor(color);c.drawCircle(x,y,dp(35),p);p.setColor(Color.WHITE);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(10));c.drawText(label,x,y+dp(4),p);}
        private void drawPiece(Canvas c,int pl,int k,float cx,float cy,float r){int pos=pieces[pl][k];int color=pl==0?0xffff3348:0xffffb400;float x,y;if(pos<0){x=cx+(pl==0?-r*.72f:r*.72f)+(k%2==0?-dp(13):dp(13));y=cy+(k<2?-dp(14):dp(14));}else if(pos>=56){x=cx+(pl==0?-dp(30):dp(30));y=cy+(k-1.5f)*dp(13);}else{int g=(startOffset(pl)+pos)%52;double a=-Math.PI/2+2*Math.PI*g/52;x=cx+(float)Math.cos(a)*r;y=cy+(float)Math.sin(a)*r;}p.setColor(color);c.drawCircle(x,y,dp(8),p);p.setColor(Color.WHITE);c.drawCircle(x,y,dp(3),p);}
    }
}
