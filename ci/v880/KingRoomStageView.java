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

/** Lightweight original animated room-stage backdrop; no third-party art assets. */
public class KingRoomStageView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); private final Random rnd=new Random(870);
    private final float[] stars=new float[72]; private final float[] speed=new float[24]; private String theme="Classic"; private long start=System.currentTimeMillis();
    public KingRoomStageView(Context c,String theme){super(c);this.theme=theme==null?"Classic":theme;setClickable(false);setFocusable(false);for(int i=0;i<24;i++){stars[i*3]=rnd.nextFloat();stars[i*3+1]=rnd.nextFloat();stars[i*3+2]=.4f+rnd.nextFloat()*.9f;speed[i]=.08f+rnd.nextFloat()*.18f;}}
    private int[] colors(){if("KTV".equals(theme))return new int[]{0xff102c68,0xff116d91,0xff07172d};if("PK".equals(theme))return new int[]{0xff8d1f41,0xff352d72,0xff174f91};if("Royal".equals(theme))return new int[]{0xff2e175a,0xff7650a9,0xff160c2e};if("Galaxy".equals(theme))return new int[]{0xff080b29,0xff402078,0xff100733};if("Festival".equals(theme))return new int[]{0xff6c1731,0xffd46a1e,0xff29102f};if("Ice".equals(theme))return new int[]{0xff0b3957,0xff49a9c9,0xffccefff};if("Love".equals(theme))return new int[]{0xff6b1538,0xffb43b69,0xff2a0d22};if("Neon".equals(theme))return new int[]{0xff071a2f,0xff0aa097,0xff28094d};if("Game".equals(theme))return new int[]{0xff076c59,0xff08a588,0xff073931};return new int[]{0xff006b56,0xff00a078,0xff08352e};}
    @Override protected void onDraw(Canvas c){super.onDraw(c);int w=getWidth(),h=getHeight();if(w<=0||h<=0)return;int[] cs=colors();p.setShader(new LinearGradient(0,0,w,h,cs[0],cs[2],Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(new RadialGradient(w*.5f,h*.25f,w*.7f,cs[1],Color.TRANSPARENT,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);float t=(System.currentTimeMillis()-start)/1000f;
        for(int i=0;i<24;i++){float x=stars[i*3]*w;float y=((stars[i*3+1]+t*speed[i])%1f)*h;float r=stars[i*3+2]*3f;p.setColor(i%3==0?0x55ffffff:0x33ffd768);c.drawCircle(x,y,r,p);}
        p.setColor(0x20ffffff);Path left=new Path();left.moveTo(w*.05f,0);left.lineTo(w*.48f,h*.82f);left.lineTo(w*.30f,h*.82f);left.close();c.drawPath(left,p);Path right=new Path();right.moveTo(w*.95f,0);right.lineTo(w*.52f,h*.82f);right.lineTo(w*.70f,h*.82f);right.close();c.drawPath(right,p);
        p.setColor(0x33000000);c.drawOval(w*.08f,h*.76f,w*.92f,h*.98f,p);p.setColor(0x26ffffff);c.drawOval(w*.18f,h*.79f,w*.82f,h*.94f,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3f);p.setColor(0x66ffd768);c.drawOval(w*.18f,h*.79f,w*.82f,h*.94f,p);p.setStyle(Paint.Style.FILL);
        float pulse=.7f+.3f*(float)Math.sin(t*2.1f);int alpha=Math.max(0,Math.min(255,(int)(28*pulse)));p.setColor((alpha<<24)|0x00ffd7);c.drawCircle(w*.5f,h*.42f,w*(.14f+.02f*pulse),p);
        postInvalidateDelayed(33);
    }
}