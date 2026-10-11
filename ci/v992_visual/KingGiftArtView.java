package com.kingplus.social;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** High-contrast shaded original gift-shop illustrations (not copied art). */
public final class KingGiftArtView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int index;
    private final String name;
    private final int[] palette={
        0xffff648a,0xffffcd52,0xff74d9ff,0xffac8bff,
        0xff63e2a0,0xffffa15c,0xffed76d7,0xff91c9ff
    };
    public KingGiftArtView(Context c,int i,String n){
        super(c); index=i;name=n==null?"Gift":n;
        setContentDescription("Animated KING gift: "+name);
    }
    private void star(Canvas c,float x,float y,float r,int color){
        Path path=new Path();
        for(int k=0;k<10;k++){
            float rr=k%2==0?r:r*.46f;
            double a=-Math.PI/2+k*Math.PI/5;
            float xx=x+(float)Math.cos(a)*rr;
            float yy=y+(float)Math.sin(a)*rr;
            if(k==0)path.moveTo(xx,yy);else path.lineTo(xx,yy);
        }
        path.close();p.setColor(color);c.drawPath(path,p);
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(),h=getHeight();
        if(w<=0||h<=0)return;
        float cx=w*.5f,cy=h*.43f,r=Math.min(w,h)*.29f;
        int accent=palette[Math.abs(index)%palette.length];
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(cx-r*.45f,cy-r*.58f,r*2f,
            new int[]{0xffffffff,accent,0xff211136},
            new float[]{0f,.47f,1f},Shader.TileMode.CLAMP));
        c.drawCircle(cx,cy,r*1.22f,p);p.setShader(null);
        p.setColor(0x77fff0c2);p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2f,w*.016f));c.drawCircle(cx,cy,r*1.19f,p);
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(cx-r,cy-r,cx+r,cy+r,
             new int[]{0xffffffff,accent,0xff45244a},null,Shader.TileMode.CLAMP));
        float R=r*.82f;int kind=Math.abs(index)%8;
        if(kind==0){
            for(int i=0;i<7;i++){
                double a=i*Math.PI*2/7;
                float x=cx+(float)Math.cos(a)*R*.51f,
                      y=cy+(float)Math.sin(a)*R*.51f;
                c.drawCircle(x,y,R*.36f,p);
            }
            p.setShader(null);p.setColor(0xffffe895);c.drawCircle(cx,cy,R*.34f,p);
        }else if(kind==1){
            Path heart=new Path();
            heart.moveTo(cx,cy+R*.80f);
            heart.cubicTo(cx-R*1.44f,cy-R*.12f,cx-R*.88f,cy-R*.98f,cx,cy-R*.35f);
            heart.cubicTo(cx+R*.88f,cy-R*.98f,cx+R*1.44f,cy-R*.12f,cx,cy+R*.80f);
            c.drawPath(heart,p);
        }else if(kind==2){
            Path crown=new Path();
            crown.moveTo(cx-R,cy+R*.55f);crown.lineTo(cx-R*.8f,cy-R*.42f);
            crown.lineTo(cx-R*.3f,cy);crown.lineTo(cx,cy-R);
            crown.lineTo(cx+R*.3f,cy);crown.lineTo(cx+R*.8f,cy-R*.42f);
            crown.lineTo(cx+R,cy+R*.55f);crown.close();c.drawPath(crown,p);
        }else if(kind==3){
            p.setShader(null);star(c,cx,cy,R*1.12f,accent);
        }else if(kind==4){
            Path gem=new Path();
            gem.moveTo(cx,cy-R);gem.lineTo(cx+R,cy-R*.3f);
            gem.lineTo(cx+R*.6f,cy+R);gem.lineTo(cx-R*.6f,cy+R);
            gem.lineTo(cx-R,cy-R*.3f);gem.close();c.drawPath(gem,p);
            p.setShader(null);p.setColor(0xbfffffff);p.setStrokeWidth(R*.058f);
            c.drawLine(cx,cy-R,cx,cy+R,p);
            c.drawLine(cx-R,cy-R*.3f,cx+R,cy-R*.3f,p);
        }else if(kind==5){
            Path rocket=new Path();
            rocket.moveTo(cx,cy-R);
            rocket.cubicTo(cx+R*.6f,cy-R*.23f,cx+R*.5f,cy+R*.2f,cx,cy+R*.6f);
            rocket.cubicTo(cx-R*.5f,cy+R*.2f,cx-R*.6f,cy-R*.23f,cx,cy-R);
            rocket.close();c.drawPath(rocket,p);
            p.setShader(null);p.setColor(0xeeffffff);
            c.drawCircle(cx,cy-R*.15f,R*.24f,p);
        }else if(kind==6){
            c.drawRoundRect(cx-R*.7f,cy-R*.1f,cx+R*.7f,cy+R*.8f,R*.1f,R*.1f,p);
            c.drawRect(cx-R,cy-R*.7f,cx-R*.48f,cy+R*.8f,p);
            c.drawRect(cx+R*.48f,cy-R*.7f,cx+R,cy+R*.8f,p);
        }else{
            c.drawOval(cx-R*.6f,cy-R*.9f,cx+R*.6f,cy+R*.2f,p);
            c.drawRect(cx-R*.15f,cy+R*.1f,cx+R*.15f,cy+R*.7f,p);
            c.drawRect(cx-R*.5f,cy+R*.65f,cx+R*.5f,cy+R*.8f,p);
        }
        p.setShader(null);
        p.setColor(0x72ffffff);
        c.drawOval(cx-R*.76f,cy-R*.83f,cx-R*.08f,cy-R*.49f,p);
        for(int i=0;i<5;i++){
            float x=cx+R*((i*53%13)/6f-1f)*1.2f;
            float y=cy+R*((i*17%11)/5f-1f)*1.3f;
            p.setColor(0xeefff8df);c.drawCircle(x,y,Math.max(1.3f,r*.045f),p);
        }
        p.setShader(null);p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(Math.max(10f,h*.12f));p.setColor(0xffffffff);
        String label=name.length()>10?name.substring(0,9)+"…":name;
        c.drawText(label,cx,h*.94f,p);
    }
}
