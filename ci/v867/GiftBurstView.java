package com.kingplus.social;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.Random;

/** Original KING Plus full-screen gift animation with tiered presentation. */
public class GiftBurstView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String icon, title, subtitle;
    private final int accent;
    private final float[] px = new float[42], py = new float[42], ps = new float[42];
    private float progress;
    private Runnable endAction;
    private ValueAnimator animator;

    public GiftBurstView(Context context, String icon, String title, String subtitle, int accent) {
        super(context); this.icon=icon==null||icon.isEmpty()?"🎁":icon; this.title=title==null?"Gift":title; this.subtitle=subtitle==null?"":subtitle; this.accent=accent;
        setLayerType(View.LAYER_TYPE_SOFTWARE,null); Random r=new Random((this.title+this.subtitle).hashCode());
        for(int i=0;i<px.length;i++){px[i]=r.nextFloat();py[i]=r.nextFloat();ps[i]=.55f+r.nextFloat()*1.35f;}
    }
    public void start(Runnable endAction,long durationMs){this.endAction=endAction;animator=ValueAnimator.ofFloat(0f,1f);animator.setDuration(Math.max(2000,durationMs));animator.setInterpolator(new DecelerateInterpolator());animator.addUpdateListener(a->{progress=(float)a.getAnimatedValue();invalidate();});animator.addListener(new android.animation.AnimatorListenerAdapter(){@Override public void onAnimationEnd(android.animation.Animator animation){if(GiftBurstView.this.endAction!=null)GiftBurstView.this.endAction.run();}});animator.start();}
    @Override protected void onDetachedFromWindow(){if(animator!=null)animator.cancel();super.onDetachedFromWindow();}
    @Override protected void onDraw(Canvas c){super.onDraw(c);int w=getWidth(),h=getHeight();if(w<=0||h<=0)return;float in=Math.min(1f,progress/.15f),out=progress>.80f?Math.max(0f,(1f-progress)/.20f):1f,a=in*out;float cx=w*.5f,cy=h*.43f,min=Math.min(w,h);
        glow.setShader(new RadialGradient(cx,cy,Math.max(w,h)*.58f,new int[]{alpha(accent,(int)(165*a)),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawCircle(cx,cy,Math.max(w,h)*.58f,glow);glow.setShader(null);
        paint.setTextAlign(Paint.Align.CENTER);paint.setAlpha((int)(255*a));paint.setColor(Color.WHITE);float pop=progress<.26f?.45f+(progress/.26f)*.70f:1.15f-Math.min(.15f,(progress-.26f)*.20f);paint.setTextSize(min*.23f*pop);c.drawText(icon,cx,cy,paint);
        paint.setTextSize(min*.075f);for(int i=0;i<6;i++){double ang=i*Math.PI/3+progress*5.5;float rr=min*(.23f+.06f*(float)Math.sin(progress*8+i));float x=cx+(float)Math.cos(ang)*rr,y=cy+(float)Math.sin(ang)*rr;paint.setAlpha((int)(210*a));c.drawText(icon,x,y,paint);}paint.setAlpha((int)(255*a));
        paint.setTypeface(Typeface.DEFAULT_BOLD);paint.setTextSize(Math.max(28f,w*.050f));paint.setColor(Color.WHITE);c.drawText(title,cx,cy+min*.16f,paint);paint.setTypeface(Typeface.DEFAULT);paint.setTextSize(Math.max(18f,w*.032f));paint.setColor(0xffffefad);c.drawText(subtitle,cx,cy+min*.215f,paint);
        paint.setColor(Color.WHITE);for(int i=0;i<px.length;i++){float ang=(float)(Math.PI*2*px[i]+progress*.9f);float dist=min*(.10f+progress*.50f*ps[i]);float x=cx+(float)Math.cos(ang)*dist,y=cy+(float)Math.sin(ang)*dist-progress*h*.10f+(py[i]-.5f)*h*.10f;paint.setAlpha((int)(220*a*(1f-progress*.42f)));float r=2.3f+7.5f*ps[i]*(1f-progress*.32f);c.drawCircle(x,y,r,paint);}paint.setAlpha(255);
        if(title.startsWith("MYTHIC")||title.startsWith("ROYAL")){paint.setTextSize(min*.055f);paint.setTypeface(Typeface.DEFAULT_BOLD);paint.setColor(alpha(0xffffe17a,(int)(255*a)));c.drawText(title.startsWith("MYTHIC")?"✦ KING PLUS MYTHIC GIFT ✦":"♛ KING PLUS ROYAL GIFT ♛",cx,h*.20f,paint);paint.setTypeface(Typeface.DEFAULT);}
    }
    private static int alpha(int color,int alpha){return (color&0x00ffffff)|(Math.max(0,Math.min(255,alpha))<<24);}
}
