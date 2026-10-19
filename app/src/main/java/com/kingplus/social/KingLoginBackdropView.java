package com.kingplus.social;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.Random;

/** Animated independent KING Plus royal night login. No proprietary assets. */
public final class KingLoginBackdropView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] specks=new float[126];
    private final long started=android.os.SystemClock.uptimeMillis();
    public KingLoginBackdropView(Context c){
        super(c);
        Random r=new Random(9911022);
        for(int i=0;i<42;i++){
            specks[i*3]=r.nextFloat();specks[i*3+1]=r.nextFloat();
            specks[i*3+2]=.5f+r.nextFloat()*1.5f;
        }
        setClickable(false);setFocusable(false);
    }
    private void glow(Canvas c,float x,float y,float radius,int center){
        p.setShader(new RadialGradient(x,y,radius,new int[]{center,0x00000000},null,Shader.TileMode.CLAMP));
        c.drawCircle(x,y,radius,p);p.setShader(null);
    }
    @Override protected void onWindowVisibilityChanged(int visibility){
        super.onWindowVisibilityChanged(visibility);
        if(visibility==View.VISIBLE)invalidate();
    }
    @Override protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        invalidate();
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(),h=getHeight();
        if(w<=0||h<=0)return;
        float t=(android.os.SystemClock.uptimeMillis()-started)/1000f;
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0,0,w,h,
          new int[]{0xff070c29,0xff25104d,0xff591e68,0xff070f2e},
          new float[]{0,.38f,.65f,1f},Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p);p.setShader(null);
        glow(c,w*.52f,h*.25f,w*.71f,0x967a52ff);
        glow(c,w*.42f,h*.43f,w*.48f,0x4848c9ff);
        glow(c,w*.8f,h*.76f,w*.4f,0x30ff4cae);
        for(int i=0;i<42;i++){
            float sx=specks[i*3]*w;
            float sy=((specks[i*3+1]-t*.013f*(.4f+specks[i*3+2]))%1f+1f)%1f*h;
            float rad=w*.0038f*specks[i*3+2];
            float pulse=(float)Math.sin(t*1.9f+i)*.22f+.72f;
            p.setColor(i%5==0?(int)(pulse*175)<<24|0x00ffe6a2:
                ((int)(pulse*146)<<24)|0x00f5fbff);
            c.drawCircle(sx,sy,rad,p);
        }
        // Suspended golden crown jewel, shaded to mimic a polished 3D emblem.
        float cx=w*.5f,cy=h*.19f,rad=Math.min(w,h)*.17f;
        glow(c,cx,cy,rad*1.75f,0x79ffd08a);
        p.setShader(new RadialGradient(cx-rad*.4f,cy-rad*.5f,rad*1.9f,
          new int[]{0xffffffd6,0xffffd976,0xff70438e,0xff120e39},
          new float[]{0,.28f,.69f,1f},Shader.TileMode.CLAMP));
        c.drawCircle(cx,cy,rad,p);p.setShader(null);
        p.setColor(0xaaffefba);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2,rad*.07f));c.drawCircle(cx,cy,rad,p);p.setStyle(Paint.Style.FILL);
        c.save();c.rotate((float)Math.sin(t*1.1)*4f,cx,cy);
        Path crown=new Path();
        crown.moveTo(cx-rad*.75f,cy+rad*.27f);
        crown.lineTo(cx-rad*.7f,cy-rad*.34f);
        crown.lineTo(cx-rad*.34f,cy-rad*.03f);
        crown.lineTo(cx,cy-rad*.7f);
        crown.lineTo(cx+rad*.34f,cy-rad*.03f);
        crown.lineTo(cx+rad*.7f,cy-rad*.34f);
        crown.lineTo(cx+rad*.75f,cy+rad*.27f);
        crown.close();
        p.setShader(new LinearGradient(cx,cy-rad,cx,cy+rad,
          new int[]{0xffffffff,0xffffe482,0xffb8782a},null,Shader.TileMode.CLAMP));
        c.drawPath(crown,p);p.setShader(null);
        p.setColor(0xffffe6a6);c.drawRoundRect(cx-rad*.75f,cy+rad*.22f,
               cx+rad*.75f,cy+rad*.4f,rad*.1f,rad*.1f,p);
        for(int i=-1;i<=1;i++){
            p.setColor(0xffc543a7);c.drawCircle(cx+i*rad*.44f,cy+rad*.30f,rad*.085f,p);
            p.setColor(Color.WHITE);c.drawCircle(cx+i*rad*.44f-rad*.024f,cy+rad*.28f,rad*.026f,p);
        }
        c.restore();

        // Ambient stage frame keeps the sign-in controls readable and centered.
        p.setColor(0x2effe0ff);
        Path l=new Path();l.moveTo(0,h*.53f);l.cubicTo(w*.10f,h*.48f,w*.17f,h*.63f,w*.25f,h*.61f);
        l.lineTo(w*.24f,h*.98f);l.lineTo(0,h);l.close();c.drawPath(l,p);
        Path r=new Path();r.moveTo(w,h*.53f);r.cubicTo(w*.90f,h*.48f,w*.83f,h*.63f,w*.75f,h*.61f);
        r.lineTo(w*.76f,h*.98f);r.lineTo(w,h);r.close();c.drawPath(r,p);
        p.setColor(0x449fe6ff);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.6f);
        c.drawOval(w*.1f,h*.84f,w*.9f,h*1.10f,p);p.setStyle(Paint.Style.FILL);
        if(isAttachedToWindow()&&isShown()&&getWindowVisibility()==View.VISIBLE)postInvalidateDelayed(170);
    }
}
