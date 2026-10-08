package com.kingplus.social;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

public final class KingPartyAmbientView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String theme;
    private long start=android.os.SystemClock.elapsedRealtime();
    private boolean running=true;

    public KingPartyAmbientView(Context c){this(c,"Classic");}
    public KingPartyAmbientView(Context c,String t){
        super(c);theme=t==null?"Classic":t;
        setClickable(false);setFocusable(false);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();running=true;postInvalidateOnAnimation();}
    @Override protected void onDetachedFromWindow(){running=false;super.onDetachedFromWindow();}

    private int[] palette(){
        if("KTV".equalsIgnoreCase(theme))return new int[]{0x6652bfff,0x442f4cff,0x00101a40};
        if("PK".equalsIgnoreCase(theme))return new int[]{0x66ff315c,0x444b1ca8,0x00130b20};
        if("Love".equalsIgnoreCase(theme))return new int[]{0x66ff5f9e,0x44aa3d8b,0x001d0d27};
        if("Game".equalsIgnoreCase(theme))return new int[]{0x6650e89b,0x443e66ff,0x000d2420};
        if("Royal".equalsIgnoreCase(theme))return new int[]{0x66ffd45c,0x445b36b5,0x00170f2b};
        if("Neon".equalsIgnoreCase(theme))return new int[]{0x6674fff7,0x44ff4fce,0x00061224};
        if("Galaxy".equalsIgnoreCase(theme))return new int[]{0x666a5cff,0x44411a76,0x00080722};
        if("Festival".equalsIgnoreCase(theme))return new int[]{0x66ff9d31,0x44e33888,0x001e1020};
        if("Ice".equalsIgnoreCase(theme))return new int[]{0x668eeeff,0x444782d6,0x000d2030};
        return new int[]{0x553d66ff,0x332e1b78,0x00000000};
    }
    private int accent(){
        if("Royal".equalsIgnoreCase(theme))return 0xffffd96b;
        if("Love".equalsIgnoreCase(theme)||"Festival".equalsIgnoreCase(theme))return 0xffff65ad;
        if("Game".equalsIgnoreCase(theme))return 0xff60f0a8;
        if("Ice".equalsIgnoreCase(theme)||"KTV".equalsIgnoreCase(theme))return 0xff73e8ff;
        if("PK".equalsIgnoreCase(theme))return 0xffff536e;
        if("Neon".equalsIgnoreCase(theme))return 0xff68fff5;
        return 0xff9a75ff;
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
        float t=(android.os.SystemClock.elapsedRealtime()-start)/1000f;
        int[] colors=palette();
        p.setShader(new RadialGradient(w*.28f,h*.20f,Math.max(w,h)*.78f,colors,new float[]{0f,.5f,1f},Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p);p.setShader(null);
        int a=accent();
        for(int i=0;i<18;i++){
            float x=(float)((i*79.0+t*(7+i%6))%Math.max(1,w));
            float base=(i*101)%Math.max(1,(int)h);
            float y=(base-t*(4+i%5))%h;if(y<0)y+=h;
            p.setColor((0x20<<24)|(a&0x00ffffff));c.drawCircle(x,y,1.4f+(i%4)*.75f,p);
        }
        float pulse=(float)(.5+.5*Math.sin(t*2.0));
        p.setColor((0x35<<24)|(a&0x00ffffff));c.drawCircle(w*.5f,h*.41f,18+14*pulse,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.2f);
        p.setColor((0x48<<24)|(a&0x00ffffff));c.drawCircle(w*.5f,h*.41f,34+20*pulse,p);
        p.setStyle(Paint.Style.FILL);
        if(running)postInvalidateOnAnimation();
    }
}
