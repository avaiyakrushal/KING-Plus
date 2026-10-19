package com.kingplus.social;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

import java.util.Random;

/** Original animated presentation panel for KING Plus parity screens. */
public class KingPresentationView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random rnd=new Random(890);
    private final float[] dots=new float[60];
    private String mode;
    private long start=System.currentTimeMillis();

    public KingPresentationView(Context c,String mode){
        super(c);this.mode=mode==null?"home":mode;
        for(int i=0;i<20;i++){dots[i*3]=rnd.nextFloat();dots[i*3+1]=rnd.nextFloat();dots[i*3+2]=.5f+rnd.nextFloat()*1.2f;}
    }
    private int[] colors(){
        if("ktv".equals(mode))return new int[]{0xff10295e,0xff137f9c,0xff07162d};
        if("pk".equals(mode))return new int[]{0xff8f1739,0xff38286f,0xff174d91};
        if("family".equals(mode))return new int[]{0xff4b255e,0xff8a4c92,0xff21102d};
        if("rank".equals(mode))return new int[]{0xff3d245d,0xff7654a6,0xff1a0d2e};
        if("gift".equals(mode))return new int[]{0xff5a184e,0xffa7437d,0xff21102d};
        if("arcade".equals(mode))return new int[]{0xff073b55,0xff087b87,0xff082139};
        return new int[]{0xff251442,0xff6d39a3,0xff120820};
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);int w=getWidth(),h=getHeight();if(w<=0||h<=0)return;float t=(System.currentTimeMillis()-start)/1000f;int[] cs=colors();
        p.setShader(new LinearGradient(0,0,w,h,cs[0],cs[2],Shader.TileMode.CLAMP));c.drawRoundRect(0,0,w,h,28,28,p);
        p.setShader(new RadialGradient(w*.5f,h*.3f,w*.7f,cs[1],Color.TRANSPARENT,Shader.TileMode.CLAMP));c.drawRoundRect(0,0,w,h,28,28,p);p.setShader(null);
        for(int i=0;i<20;i++){float x=dots[i*3]*w;float y=((dots[i*3+1]+t*(.025f+.006f*i))%1f)*h;float r=dots[i*3+2]*2.2f;p.setColor(i%2==0?0x55ffffff:0x44ffd86a);c.drawCircle(x,y,r,p);}
        if("ktv".equals(mode))drawKtv(c,w,h,t);else if("pk".equals(mode))drawPk(c,w,h,t);else if("family".equals(mode))drawFamily(c,w,h,t);else if("rank".equals(mode))drawRank(c,w,h,t);else if("gift".equals(mode))drawGift(c,w,h,t);else if("arcade".equals(mode))drawArcade(c,w,h,t);else drawHome(c,w,h,t);
        postInvalidateDelayed(33);
    }
    private void drawKtv(Canvas c,int w,int h,float t){
        p.setStrokeWidth(6);p.setStrokeCap(Paint.Cap.ROUND);
        for(int i=0;i<11;i++){float x=w*.13f+i*w*.067f;float amp=(float)(.18+.28*Math.abs(Math.sin(t*2+i*.65)));p.setColor(i%2==0?0x99ffffff:0xffffd768);c.drawLine(x,h*.62f-amp*h*.35f,x,h*.62f+amp*h*.18f,p);}
        p.setColor(0x44ffffff);c.drawCircle(w*.5f,h*.48f,h*.26f,p);p.setColor(0xffffd768);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(h*.28f);c.drawText("🎤",w*.5f,h*.57f,p);
    }
    private void drawPk(Canvas c,int w,int h,float t){
        p.setColor(0x55ff315a);c.drawRect(0,0,w*.48f,h,p);p.setColor(0x554880ff);c.drawRect(w*.52f,0,w,h,p);
        p.setColor(0xffffd768);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(h*.25f);c.drawText("VS",w*.5f,h*.56f,p);
        float pulse=1f+(float)Math.sin(t*3)*.08f;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setColor(0xaaffd768);c.drawCircle(w*.5f,h*.49f,h*.23f*pulse,p);p.setStyle(Paint.Style.FILL);
        p.setTextSize(h*.22f);p.setColor(Color.WHITE);c.drawText("🔥",w*.22f,h*.58f,p);c.drawText("❄️",w*.78f,h*.58f,p);
    }
    private void drawFamily(Canvas c,int w,int h,float t){
        p.setColor(0x55ffd768);c.drawCircle(w*.5f,h*.5f,h*.34f,p);p.setColor(0x44ffffff);c.drawCircle(w*.5f,h*.5f,h*.26f,p);
        p.setTextAlign(Paint.Align.CENTER);p.setTextSize(h*.32f);p.setColor(Color.WHITE);c.drawText("👑",w*.5f,h*.57f,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(0xaaffd768);c.drawCircle(w*.5f,h*.5f,h*(.35f+.02f*(float)Math.sin(t*2)),p);p.setStyle(Paint.Style.FILL);
    }
    private void drawRank(Canvas c,int w,int h,float t){
        float base=h*.82f;float bw=w*.16f;p.setColor(0x88c7c7d9);c.drawRoundRect(w*.22f,base-h*.31f,w*.22f+bw,base,14,14,p);p.setColor(0xbbffd768);c.drawRoundRect(w*.42f,base-h*.48f,w*.42f+bw,base,14,14,p);p.setColor(0x88d38a63);c.drawRoundRect(w*.62f,base-h*.23f,w*.62f+bw,base,14,14,p);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(h*.18f);p.setColor(Color.WHITE);c.drawText("2",w*.30f,base-h*.15f,p);c.drawText("1",w*.50f,base-h*.27f,p);c.drawText("3",w*.70f,base-h*.10f,p);
    }
    private void drawGift(Canvas c,int w,int h,float t){
        p.setTextAlign(Paint.Align.CENTER);p.setTextSize(h*.34f);p.setColor(Color.WHITE);c.drawText("🎁",w*.5f,h*.58f,p);
        for(int i=0;i<10;i++){double a=t*1.5+i*.63;float r=h*(.20f+.025f*i);float x=w*.5f+(float)Math.cos(a)*r;float y=h*.5f+(float)Math.sin(a)*r*.55f;p.setColor(i%2==0?0xffffd768:0xffff73c8);c.drawCircle(x,y,4+(i%3),p);}
    }
    private void drawArcade(Canvas c,int w,int h,float t){
        String[] e={"🎲","⭕","🐺","🎨","🎰","🎯"};p.setTextAlign(Paint.Align.CENTER);p.setTextSize(h*.22f);for(int i=0;i<6;i++){float x=w*(.17f+(i%3)*.33f),y=h*(.33f+(i/3)*.40f);p.setColor(0x30ffffff);c.drawRoundRect(x-w*.11f,y-h*.18f,x+w*.11f,y+h*.14f,18,18,p);p.setColor(Color.WHITE);c.drawText(e[i],x,y,p);}}
    private void drawHome(Canvas c,int w,int h,float t){p.setTextAlign(Paint.Align.CENTER);p.setTextSize(h*.34f);p.setColor(Color.WHITE);c.drawText("👑",w*.5f,h*.59f,p);}
}