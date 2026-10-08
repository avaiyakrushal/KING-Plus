package com.kingplus.social;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

/** Lightweight original KING Plus animated room atmosphere; no third-party assets. */
public final class KingPartyAmbientView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private long start=android.os.SystemClock.elapsedRealtime();
    private boolean running=true;
    public KingPartyAmbientView(Context c){super(c);setClickable(false);setFocusable(false);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();running=true;postInvalidateOnAnimation();}
    @Override protected void onDetachedFromWindow(){running=false;super.onDetachedFromWindow();}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(),h=getHeight(); if(w<=0||h<=0)return;
        float t=(android.os.SystemClock.elapsedRealtime()-start)/1000f;
        p.setShader(new RadialGradient(w*.24f,h*.18f,Math.max(w,h)*.72f,
            new int[]{0x553d66ff,0x332e1b78,0x00000000},
            new float[]{0f,.48f,1f}, Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p);p.setShader(null);
        p.setColor(0x22ffffff);
        for(int i=0;i<18;i++){
            float x=(float)((i*73.0 + t*(8+i%5))%Math.max(1,w));
            float base=(i*97)%Math.max(1,(int)h);
            float y=(base - t*(5+i%4))%h;if(y<0)y+=h;
            float r=1.5f+(i%4)*.7f;
            p.setAlpha(26+(i%5)*7);c.drawCircle(x,y,r,p);
        }
        p.setAlpha(255);
        float pulse=(float)(.5+.5*Math.sin(t*2.1));
        p.setColor(0x553fe8ff);
        c.drawCircle(w*.5f,h*.42f,18+14*pulse,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2f);
        p.setColor(0x448b5cff);
        c.drawCircle(w*.5f,h*.42f,34+18*pulse,p);
        p.setStyle(Paint.Style.FILL);
        if(running)postInvalidateOnAnimation();
    }
}
