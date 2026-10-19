package com.kingplus.social;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import java.util.Random;

/** KING original cinematic pseudo-3D gifts, drawn locally. No third-party art.
 * Works on free decorative Gift events only, with NO payment or wallet mutation.
 */
public final class GiftBurstView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String icon,title,subtitle;
    private final int accent;
    private final float[] confettiX=new float[68],confettiY=new float[68],
        confettiSpeed=new float[68],confettiSize=new float[68];
    private ValueAnimator animator;
    private float progress=0f;
    private Runnable completed;
    private boolean released=false;
    public GiftBurstView(Context c,String icon,String title,String subtitle,int accent){
        super(c);
        this.icon=icon==null||icon.isEmpty()?"🎁":icon;
        this.title=title==null?"KING Gift":title;
        this.subtitle=subtitle==null?"":subtitle;
        this.accent=accent;
        Random r=new Random((this.title+this.icon).hashCode());
        for(int i=0;i<68;i++){
            confettiX[i]=r.nextFloat()*2-1;
            confettiY[i]=r.nextFloat()*2-1;
            confettiSpeed[i]=.55f+r.nextFloat()*1.15f;
            confettiSize[i]=.6f+r.nextFloat()*1.5f;
        }
        setClickable(false);
        setFocusable(false);
        setContentDescription("Animated gift: "+this.title);
    }
    public void start(Runnable endAction,long durationMs){
        if(animator!=null)animator.cancel();
        completed=endAction;released=false;
        animator=ValueAnimator.ofFloat(0f,1f);
        animator.setDuration(Math.max(2300,Math.min(5500,durationMs)));
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a->{progress=(Float)a.getAnimatedValue();invalidate();});
        animator.addListener(new AnimatorListenerAdapter(){
            @Override public void onAnimationEnd(Animator a){
                if(!released){released=true;if(completed!=null)completed.run();}
            }
        });
        animator.start();
    }
    @Override protected void onDetachedFromWindow(){
        released=true;
        if(animator!=null)animator.cancel();
        super.onDetachedFromWindow();
    }
    private static int tinted(int color,float alpha){
        return (color&0x00ffffff)|((int)(255*Math.max(0,Math.min(1,alpha)))<<24);
    }
    private void drawLight(Canvas c,float x,float y,float radius,int color,float alpha){
        if(radius<=1)return;
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(x,y,radius,
            new int[]{tinted(Color.WHITE,alpha*.75f),tinted(color,alpha*.34f),0},
            new float[]{0f,.38f,1f},Shader.TileMode.CLAMP));
        c.drawCircle(x,y,radius,p);
        p.setShader(null);
    }
    private void star(Canvas c,float x,float y,float radius,float opacity){
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1.4f,radius*.13f));
        p.setColor(tinted(0xfffff6c7,opacity));
        c.drawLine(x-radius,y,x+radius,y,p);
        c.drawLine(x,y-radius,x,y+radius,p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(tinted(Color.WHITE,opacity));
        c.drawCircle(x,y,Math.max(1.2f,radius*.16f),p);
    }
    private void centerText(Canvas c,String text,float x,float y,float size,int col,boolean strong,float maxWidth){
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        p.setTypeface(strong?Typeface.create("sans-serif-black",Typeface.BOLD):Typeface.DEFAULT);
        p.setTextSize(size);
        p.setTextAlign(Paint.Align.CENTER);
        p.setColor(col);
        p.setShadowLayer(2.5f,0,2f,0x88000000);
        String s=text;
        while(s.length()>8 && p.measureText(s)>maxWidth)s=s.substring(0,s.length()-2)+"…";
        c.drawText(s,x,y,p);
        p.clearShadowLayer();
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(),h=getHeight();
        if(w<1||h<1)return;
        float min=Math.min(w,h),cx=w*.5f,cy=h*.43f;
        float fadeIn=Math.min(1f,progress/.12f);
        float fadeOut=progress>.82f?Math.max(0,(1f-progress)/.18f):1f;
        float alpha=fadeIn*fadeOut;
        if(alpha<=.001f)return;
        p.setStyle(Paint.Style.FILL);
        // Dark cinema scrim: leaves the underlying Party visible.
        p.setColor(tinted(0xff08031a,alpha*.76f)); c.drawRect(0,0,w,h,p);
        drawLight(c,cx,cy,min*.66f,accent,alpha);
        drawLight(c,cx,cy-min*.11f,min*.4f,0xffffd67a,alpha*.8f);

        // Rotating golden concentric halos and volumetric spotlight.
        float rr=min*.35f;
        for(int i=0;i<3;i++){
            p.setShader(null);
            p.setColor(tinted(i==0?accent:0xffffe69e,alpha*(.61f-i*.15f)));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1.5f,min*.008f*(3-i)));
            c.save();c.rotate((progress*230*(i%2==0?1:-1))+i*30,cx,cy);
            c.drawOval(cx-rr*(1f+i*.18f),cy-rr*.58f*(1f+i*.18f),
                       cx+rr*(1f+i*.18f),cy+rr*.58f*(1f+i*.18f),p);
            c.restore();
        }
        p.setStyle(Paint.Style.FILL);

        // Large jewel capsule shaded with radial gradients and a specular gloss.
        float pop=progress<.22f?(float)(.5+.65*progress/.22):1.15f-.15f*Math.min(1f,(progress-.22f)*4);
        float radius=min*.255f*pop;
        p.setShader(null);
        p.setColor(tinted(0xff050015,alpha*.62f));
        c.drawOval(cx-radius*.85f,cy+radius*.66f,cx+radius*.85f,cy+radius*.99f,p);
        p.setShader(new RadialGradient(cx-radius*.38f,cy-radius*.46f,
           radius*1.86f,
           new int[]{tinted(0xffffffff,alpha),tinted(0xffffe69e,alpha),
              tinted(accent,alpha),tinted(0xff170831,alpha)},
           new float[]{0f,.2f,.65f,1f},Shader.TileMode.CLAMP));
        c.drawCircle(cx,cy,radius,p);
        p.setShader(null);
        p.setColor(tinted(0xffffffff,alpha*.51f));
        c.drawOval(cx-radius*.58f,cy-radius*.79f,cx+radius*.08f,cy-radius*.43f,p);
        p.setColor(tinted(0xfff5d990,alpha*.95f));
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(min*.009f);
        c.drawCircle(cx,cy,radius,p);p.setStyle(Paint.Style.FILL);

        // Perspective rotation simulates depth without a heavy game engine.
        Camera cam=new Camera();
        Matrix m=new Matrix();
        cam.save();cam.rotateY((float)Math.sin(progress*9)*17f);
        cam.rotateX((float)Math.sin(progress*6)*7f);
        cam.getMatrix(m);cam.restore();
        m.preTranslate(-cx,-cy);m.postTranslate(cx,cy);
        c.save();c.concat(m);
        p.setShader(null);p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(radius*1.57f);
        p.setColor(tinted(Color.WHITE,alpha));
        p.setShadowLayer(radius*.13f,0,radius*.11f,0x99000000);
        c.drawText(icon,cx,cy+radius*.53f,p);
        p.clearShadowLayer();c.restore();

        // Upward bursting glitter and moving confetti.
        for(int i=0;i<68;i++){
            float dist=min*(.12f+progress*.78f*confettiSpeed[i]);
            float x=cx+confettiX[i]*dist;
            float y=cy+confettiY[i]*dist-progress*h*.13f;
            float a=alpha*(1f-progress*.53f);
            float r=min*.0045f*confettiSize[i]*(1f-progress*.4f);
            p.setColor(tinted(i%4==0?Color.WHITE:i%4==1?accent:i%4==2?0xffffeb95:0xff82deff,a));
            if(i%6==0)star(c,x,y,r*2.8f,a);else c.drawCircle(x,y,r,p);
        }

        if(title.startsWith("MYTHIC")||title.startsWith("ROYAL")){
            centerText(c,"✦ KING PLUS • CINEMATIC GIFT ✦",cx,h*.20f,
              Math.max(13f,w*.039f),tinted(0xffffe69e,alpha),true,w*.92f);
        }
        centerText(c,title,cx,cy+radius+min*.125f,
           Math.max(19f,w*.055f),tinted(Color.WHITE,alpha),true,w*.94f);
        centerText(c,subtitle,cx,cy+radius+min*.196f,
           Math.max(13f,w*.038f),tinted(0xffffedb7,alpha),false,w*.92f);
        if(progress<.84f){
            centerText(c,"✦  LIVE ROOM GIFT  ✦",cx,h*.88f,
                Math.max(14f,w*.043f),tinted(accent,alpha*.85f),true,w*.88f);
        }
    }
}
