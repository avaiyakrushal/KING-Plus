package com.kingplus.social;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;
import java.util.Random;

public class KingEntranceView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random rnd=new Random();
    private final float[] px=new float[24],py=new float[24],speed=new float[24];
    private final String name,effect,frame;
    private float progress;
    public KingEntranceView(Context c,String name,String effect,String frame){super(c);this.name=name==null?"Guest":name;this.effect=effect==null?"Welcome Sparkle":effect;this.frame=frame==null?"Minimal Frame":frame;p.setTypeface(Typeface.DEFAULT_BOLD);for(int i=0;i<px.length;i++){px[i]=rnd.nextFloat();py[i]=rnd.nextFloat();speed[i]=.5f+rnd.nextFloat()*1.5f;}setClickable(false);}
    public void start(Runnable done){ValueAnimator a=ValueAnimator.ofFloat(0f,1f);a.setDuration("Royal Arrival".equals(effect)?3400:2800);a.addUpdateListener(v->{progress=(float)v.getAnimatedValue();invalidate();});a.addListener(new android.animation.AnimatorListenerAdapter(){@Override public void onAnimationEnd(android.animation.Animator animation){if(done!=null)done.run();}});a.start();}
    @Override protected void onDraw(Canvas c){super.onDraw(c);int w=getWidth(),h=getHeight();if(w<=0||h<=0)return;float alpha=progress<.15f?progress/.15f:progress>.78f?(1f-progress)/.22f:1f;alpha=Math.max(0,Math.min(1,alpha));
        p.setColor(((int)(90*alpha)<<24)|0x12091f);c.drawRect(0,0,w,h,p);
        String particle=KingCosmetics.effectEmoji(effect);if(particle.isEmpty())particle="✨";p.setTextSize(Math.max(24,w/18f));p.setTextAlign(Paint.Align.CENTER);
        for(int i=0;i<px.length;i++){float x=px[i]*w;float y=((py[i]-progress*speed[i])%1f+1f)%1f*h;p.setColor(Color.argb((int)(220*alpha),255,220-(i%3)*25,255));c.drawText(particle,x,y,p);}
        float cx=w/2f,cy=h*.48f;float ring=Math.min(w,h)*(.16f+.06f*(float)Math.sin(progress*Math.PI));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(5,w/90f));p.setColor(Color.argb((int)(240*alpha),255,207,71));c.drawCircle(cx,cy,ring,p);p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.CENTER);p.setTextSize(Math.max(30,w/12f));p.setColor(Color.argb((int)(255*alpha),255,255,255));c.drawText(KingCosmetics.frameEmoji(frame),cx,cy-ring*.20f,p);
        p.setTextSize(Math.max(24,w/18f));c.drawText(name,cx,cy+ring*.35f,p);p.setTextSize(Math.max(15,w/30f));p.setColor(Color.argb((int)(240*alpha),255,224,125));c.drawText(effect+" • "+frame,cx,cy+ring*.67f,p);
    }
}
