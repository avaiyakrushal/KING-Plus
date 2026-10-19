package com.kingplus.social;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.Locale;

/** Original KING Plus game-card artwork; no third-party game assets are bundled. */
public final class KingGameArtView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String code,title;
    public KingGameArtView(Context c,String game,String label){super(c);code=game==null?"game":game.toLowerCase(Locale.US);title=label==null?"KING GAME":label;setContentDescription(title);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();int[] cs=colors();p.setShader(new LinearGradient(0,0,w,h,cs[0],cs[1],Shader.TileMode.CLAMP));c.drawRoundRect(0,0,w,h,24,24,p);p.setShader(null);p.setColor(0x28ffffff);c.drawCircle(w*.82f,h*.16f,w*.25f,p);c.drawCircle(w*.08f,h*.98f,w*.32f,p);
        if(code.contains("ludo"))ludo(c,w,h);else if(code.contains("wolf"))wolf(c,w,h);else if(code.contains("spy"))spy(c,w,h);else if(code.contains("draw"))draw(c,w,h);else if(code.contains("bingo")||code.contains("guess"))numbers(c,w,h);else if(code.contains("domino"))domino(c,w,h);else if(code.contains("memory"))memory(c,w,h);else if(code.contains("slot"))slot(c,w,h);else if(code.contains("coin"))coin(c,w,h);else if(code.contains("dice"))dice(c,w,h);else if(code.contains("sheep"))sheep(c,w,h);else if(code.contains("zoo"))zoo(c,w,h);else if(code.contains("reaction"))bolt(c,w,h);else if(code.contains("wheel"))wheel(c,w,h);else generic(c,w,h);
        p.setShader(new LinearGradient(0,h*.62f,0,h,0x00000000,0x99000000,Shader.TileMode.CLAMP));c.drawRect(0,h*.55f,w,h,p);p.setShader(null);p.setTextAlign(Paint.Align.LEFT);p.setTypeface(Typeface.DEFAULT_BOLD);p.setColor(Color.WHITE);p.setTextSize(Math.max(12f,h*.13f));String n=title.length()>18?title.substring(0,18):title;c.drawText(n,w*.07f,h*.90f,p);
    }
    private int[] colors(){int k=Math.abs(code.hashCode())%7;int[][] a={{0xff3478d4,0xff172b64},{0xffa85de3,0xff40225d},{0xff22a17f,0xff12473e},{0xffe36c79,0xff69273a},{0xffe69a39,0xff704219},{0xff16a8c2,0xff154558},{0xff7562e6,0xff312b78}};return a[k];}
    private void ludo(Canvas c,float w,float h){float s=Math.min(w,h)*.20f,cx=w*.5f,cy=h*.42f;int[] co={0xffff5b62,0xff43d37a,0xffffdb4a,0xff4da3ff};for(int i=0;i<4;i++){float x=cx+(i%2==0?-s:s),y=cy+(i<2?-s:s);p.setColor(co[i]);c.drawCircle(x,y,s*.78f,p);p.setColor(Color.WHITE);c.drawCircle(x,y,s*.26f,p);}p.setColor(0xddffffff);c.drawRoundRect(cx-s*.38f,cy-s*.38f,cx+s*.38f,cy+s*.38f,12,12,p);}
    private void wolf(Canvas c,float w,float h){p.setColor(0xffdce8ff);Path q=new Path();q.moveTo(w*.5f,h*.17f);q.lineTo(w*.35f,h*.37f);q.lineTo(w*.39f,h*.68f);q.lineTo(w*.5f,h*.78f);q.lineTo(w*.61f,h*.68f);q.lineTo(w*.65f,h*.37f);q.close();c.drawPath(q,p);p.setColor(0xff1f2940);c.drawCircle(w*.45f,h*.48f,5,p);c.drawCircle(w*.55f,h*.48f,5,p);}
    private void spy(Canvas c,float w,float h){p.setColor(0xeef5f2ff);c.drawCircle(w*.5f,h*.42f,h*.22f,p);p.setColor(0xff29223a);c.drawRect(w*.36f,h*.30f,w*.64f,h*.36f,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(6);c.drawCircle(w*.45f,h*.42f,10,p);c.drawCircle(w*.55f,h*.42f,10,p);c.drawLine(w*.45f,h*.42f,w*.55f,h*.42f,p);p.setStyle(Paint.Style.FILL);}
    private void draw(Canvas c,float w,float h){p.setColor(0xeeffffff);c.drawRoundRect(w*.27f,h*.20f,w*.70f,h*.64f,12,12,p);p.setColor(0xffff6f73);p.setStrokeWidth(9);c.drawLine(w*.35f,h*.55f,w*.60f,h*.31f,p);p.setColor(0xff6e53d8);c.drawCircle(w*.61f,h*.30f,7,p);}
    private void numbers(Canvas c,float w,float h){p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(h*.28f);p.setColor(Color.WHITE);c.drawText("7",w*.35f,h*.50f,p);p.setColor(0xffffd54a);c.drawText("24",w*.60f,h*.62f,p);}
    private void domino(Canvas c,float w,float h){p.setColor(0xeeffffff);c.rotate(-14,w*.5f,h*.42f);c.drawRoundRect(w*.35f,h*.18f,w*.65f,h*.67f,14,14,p);p.setColor(0xff27233a);c.drawLine(w*.35f,h*.425f,w*.65f,h*.425f,p);for(int i=0;i<3;i++){c.drawCircle(w*(.43f+i*.07f),h*.31f,5,p);c.drawCircle(w*(.43f+i*.07f),h*.54f,5,p);}c.rotate(14,w*.5f,h*.42f);}
    private void memory(Canvas c,float w,float h){for(int i=0;i<4;i++){p.setColor(i%2==0?0xffffd25a:0xffef8bd1);float x=w*(.30f+(i%2)*.25f),y=h*(.23f+(i/2)*.25f);c.drawRoundRect(x,y,x+w*.18f,y+h*.19f,12,12,p);}}
    private void slot(Canvas c,float w,float h){p.setColor(0xfff3f1ff);c.drawRoundRect(w*.25f,h*.23f,w*.75f,h*.62f,16,16,p);p.setColor(0xffff5570);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(h*.20f);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("777",w*.5f,h*.49f,p);}
    private void coin(Canvas c,float w,float h){p.setColor(0xffffd455);c.drawCircle(w*.5f,h*.42f,h*.22f,p);p.setColor(0xff7a5200);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(h*.23f);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("K",w*.5f,h*.50f,p);}
    private void dice(Canvas c,float w,float h){p.setColor(Color.WHITE);c.drawRoundRect(w*.34f,h*.20f,w*.67f,h*.62f,17,17,p);p.setColor(0xff302843);float[][] d={{.41f,.30f},{.59f,.30f},{.50f,.41f},{.41f,.53f},{.59f,.53f}};for(float[] x:d)c.drawCircle(w*x[0],h*x[1],6,p);}
    private void sheep(Canvas c,float w,float h){p.setColor(0xfff8f7ff);for(int i=0;i<5;i++)c.drawCircle(w*(.40f+i*.05f),h*(.34f+(i%2)*.06f),h*.10f,p);p.setColor(0xff4c4055);c.drawCircle(w*.65f,h*.43f,h*.10f,p);}
    private void zoo(Canvas c,float w,float h){p.setColor(0xffffd06a);c.drawCircle(w*.5f,h*.40f,h*.20f,p);p.setColor(0xffb36b2d);for(int i=0;i<10;i++){double a=Math.PI*2*i/10;c.drawCircle(w*.5f+(float)Math.cos(a)*h*.22f,h*.40f+(float)Math.sin(a)*h*.22f,h*.055f,p);}p.setColor(0xff3b2d20);c.drawCircle(w*.44f,h*.38f,4,p);c.drawCircle(w*.56f,h*.38f,4,p);}
    private void bolt(Canvas c,float w,float h){p.setColor(0xffffe049);Path q=new Path();q.moveTo(w*.55f,h*.15f);q.lineTo(w*.36f,h*.43f);q.lineTo(w*.48f,h*.43f);q.lineTo(w*.39f,h*.70f);q.lineTo(w*.66f,h*.36f);q.lineTo(w*.53f,h*.36f);q.close();c.drawPath(q,p);}
    private void wheel(Canvas c,float w,float h){float cx=w*.5f,cy=h*.40f,r=h*.22f;int[] co={0xffff657a,0xffffd04a,0xff66d8ff,0xffa879ff,0xff61d69b,0xffff9c52};for(int i=0;i<6;i++){p.setColor(co[i]);c.drawArc(cx-r,cy-r,cx+r,cy+r,i*60,60,true,p);}p.setColor(Color.WHITE);c.drawCircle(cx,cy,r*.18f,p);}
    private void generic(Canvas c,float w,float h){p.setColor(0xddffffff);c.drawCircle(w*.5f,h*.40f,h*.22f,p);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(h*.20f);p.setColor(0xff4c3d7a);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("KING",w*.5f,h*.47f,p);}
}
