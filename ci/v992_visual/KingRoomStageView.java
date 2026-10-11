package com.kingplus.social;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.Random;

/** KING Plus original animated premium theme backgrounds.
 * Native Canvas, no server writes, no borrowed Bolo Hi artwork.
 */
public final class KingRoomStageView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] stars=new float[144];
    private final float[] velocity=new float[48];
    private final String theme;
    private final long started=android.os.SystemClock.uptimeMillis();
    public KingRoomStageView(Context c,String theme){
        super(c);this.theme=(theme==null||theme.isEmpty())?"Classic":theme;
        setClickable(false);setFocusable(false);
        Random r=new Random(2026991L);
        for(int i=0;i<48;i++){
            stars[i*3]=r.nextFloat();stars[i*3+1]=r.nextFloat();
            stars[i*3+2]=.45f+r.nextFloat()*1.2f;
            velocity[i]=.02f+r.nextFloat()*.04f;
        }
    }
    private int[] colors(){
        switch(theme){
            case "KTV":return new int[]{0xff061a41,0xff146ca6,0xff060c30,0xff66e7ff};
            case "PK":return new int[]{0xff3b071d,0xff772b6c,0xff0a2159,0xffff617e};
            case "Love":return new int[]{0xff310e29,0xffac3f79,0xff210d3f,0xffffa9cc};
            case "Game":return new int[]{0xff042a24,0xff118d73,0xff001d42,0xff79ffd0};
            case "Royal":return new int[]{0xff1a0b3c,0xff6540a8,0xff110d2c,0xffffdc84};
            case "Neon":return new int[]{0xff04172e,0xff167c85,0xff100d30,0xff48eeff};
            case "Galaxy":return new int[]{0xff06071f,0xff40216c,0xff120e42,0xffa7a0ff};
            case "Festival":return new int[]{0xff4d102c,0xffbc4d3e,0xff280a33,0xffffd978};
            case "Ice":return new int[]{0xff083254,0xff4ba4be,0xff12324a,0xffc7f5ff};
            case "Diamond":return new int[]{0xff051934,0xff166b97,0xff110d30,0xff9cf7ff};
            case "Sakura":return new int[]{0xff37103d,0xff9c4587,0xff3d1747,0xffffb2da};
            case "Ocean":return new int[]{0xff042846,0xff076c85,0xff05132a,0xff82fbe9};
            case "Sunset":return new int[]{0xff3e1648,0xffd05c6c,0xff271344,0xffffd58c};
            case "Aurora":return new int[]{0xff051b2d,0xff0e6b66,0xff152458,0xffaaffc9};
            default:return new int[]{0xff063b3c,0xff0a927e,0xff052939,0xffb0ffdd};
        }
    }
    private void light(Canvas c,float x,float y,float radius,int color,float strength){
        p.setStyle(Paint.Style.FILL);
        int a=(int)(Math.min(1,Math.max(0,strength))*255);
        p.setShader(new RadialGradient(x,y,Math.max(2,radius),
                new int[]{(a<<24)|(color&0xffffff),0},null,Shader.TileMode.CLAMP));
        c.drawCircle(x,y,radius,p);p.setShader(null);
    }
    private void ray(Canvas c,float xTop,float xBottom,float spread,float h,int color){
        Path path=new Path();
        path.moveTo(xTop,-30);
        path.lineTo(xBottom-spread,h*.85f);
        path.lineTo(xBottom+spread,h*.85f);
        path.close();
        p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawPath(path,p);
    }
    private void facet(Canvas c,float cx,float cy,float r,int stroke){
        Path path=new Path();
        path.moveTo(cx,cy-r);
        path.lineTo(cx+r*.77f,cy-r*.39f);
        path.lineTo(cx+r*.57f,cy+r*.64f);
        path.lineTo(cx-r*.57f,cy+r*.64f);
        path.lineTo(cx-r*.77f,cy-r*.39f);
        path.close();
        p.setColor(0x1136eeff);p.setStyle(Paint.Style.FILL);c.drawPath(path,p);
        p.setColor(stroke);p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2,r*.035f));
        c.drawPath(path,p);
        c.drawLine(cx,cy-r,cx,cy+r*.64f,p);
        c.drawLine(cx-r*.77f,cy-r*.39f,cx+r*.77f,cy-r*.39f,p);
        c.drawLine(cx-r*.77f,cy-r*.39f,cx,cy+r*.64f,p);
        c.drawLine(cx+r*.77f,cy-r*.39f,cx,cy+r*.64f,p);
        p.setStyle(Paint.Style.FILL);
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(),h=getHeight();
        if(w<=0||h<=0)return;
        float t=(android.os.SystemClock.uptimeMillis()-started)/1000f;
        int[] cl=colors();
        p.setShader(new LinearGradient(0,0,w*.7f,h,
          new int[]{cl[0],cl[1],cl[2]},null,Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p);p.setShader(null);
        // High ceilings with moving light beams and depth fog.
        light(c,w*.5f,h*.24f,w*.72f,cl[1],.62f);
        light(c,w*.23f,h*.5f,w*.5f,cl[3],.13f);
        light(c,w*.81f,h*.69f,w*.46f,cl[3],.10f);
        c.save();
        c.rotate((float)Math.sin(t*.33f)*4,w*.5f,0);
        ray(c,w*.13f,w*.55f,w*.13f,h,0x15ffffff);
        ray(c,w*.88f,w*.45f,w*.13f,h,0x10ffffff);
        ray(c,w*.52f,w*.72f,w*.19f,h,0x08ffe5a0);
        c.restore();

        // Elegant layered stage floor with luminous elliptical gold border.
        p.setColor(0x7e01051c);
        c.drawOval(w*-.1f,h*.78f,w*1.1f,h*1.10f,p);
        p.setShader(new LinearGradient(0,h*.80f,0,h*.99f,
             new int[]{0x5bc4ebec,0x06221246,0x58000000},null,Shader.TileMode.CLAMP));
        c.drawOval(w*.06f,h*.81f,w*.94f,h*.99f,p);
        p.setShader(null);
        p.setColor(0x77ffdf92);p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2,w*.005f));
        c.drawOval(w*.15f,h*.82f,w*.85f,h*.965f,p);
        p.setStyle(Paint.Style.FILL);

        // Constellations / glitter, moving but lightweight.
        for(int i=0;i<48;i++){
            float x=stars[i*3]*w;
            float y=(stars[i*3+1]+t*velocity[i])%1f*h;
            float radius=Math.max(1.2f,stars[i*3+2]*w*.005f);
            float shimmer=.46f+.34f*(float)Math.sin(t*2.3f+i*.63f);
            int a=(int)(shimmer*170);
            p.setColor((a<<24)|(i%4==0?(cl[3]&0xffffff):0xffffff));
            c.drawCircle(x,y,radius,p);
            if(i%9==0){
                p.setStrokeWidth(1.2f);c.drawLine(x-radius*3,y,x+radius*3,y,p);
                c.drawLine(x,y-radius*3,x,y+radius*3,p);
            }
        }
        if("Diamond".equals(theme)||"Royal".equals(theme)){
            facet(c,w*.50f,h*.50f,w*.17f,0x77ffffff);
        }else if("Sakura".equals(theme)){
            for(int i=0;i<12;i++){
                float x=((i*.119f+t*.013f)%1f)*w, y=((i*.171f+t*.028f)%1f)*h;
                p.setColor(0x88ffbfd6);
                c.save();c.rotate(i*21+t*13,x,y);
                c.drawOval(x-8,y-4,x+8,y+4,p);c.restore();
            }
        }else if("Aurora".equals(theme)){
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(w*.085f);
            for(int i=0;i<3;i++){
                p.setColor(i==0?0x2241ffad:i==1?0x219ab4ff:0x20ffffc4);
                Path path=new Path();path.moveTo(0,h*(.3f+i*.13f));
                path.cubicTo(w*.3f,h*(.14f+i*.13f),w*.7f,h*(.5f+i*.07f),w,h*(.16f+i*.11f));
                c.drawPath(path,p);
            }
            p.setStyle(Paint.Style.FILL);
        }
        if(isAttachedToWindow())postInvalidateDelayed(60);
    }
}
