package com.kingplus.social;

import android.content.Context;
import android.graphics.*;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.view.View;

/** KING original animated aura for the 50 live emoji views.
 * The original emoji art stays in the center; a dimensional light ring
 * and glitter animate behind it. No remote assets or extra database writes.
 */
public final class KingEmojiAuraView992 extends FrameLayout {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final long born=android.os.SystemClock.uptimeMillis();
    public KingEmojiAuraView992(Context context,int emojiIndex,int durationMs){
        super(context);
        setWillNotDraw(false);
        setClipChildren(false);setClipToPadding(false);
        setClickable(false);setFocusable(false);
        LiveEmojiView core=new LiveEmojiView(context,emojiIndex,durationMs);
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER);
        addView(core,lp);
        setContentDescription("3D glow live emoji "+LiveEmojiView.LABELS[Math.max(0,Math.min(LiveEmojiView.LABELS.length-1,emojiIndex))]);
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
        float cx=w*.5f,cy=h*.5f,min=Math.min(w,h);
        float t=(android.os.SystemClock.uptimeMillis()-born)/1000f;
        float pulse=.76f+.24f*(float)Math.sin(t*5.2);
        float radius=min*.34f;
        p.setShader(new RadialGradient(cx,cy,radius*1.2f,
            new int[]{0x5dffffff,0x4dbe69ff,0x1289f6ff,0},new float[]{0,.35f,.7f,1},Shader.TileMode.CLAMP));
        c.drawCircle(cx,cy,radius*1.2f,p);p.setShader(null);
        for(int i=0;i<3;i++){
            float ang=i*60+t*90*(i%2==0?1:-1);
            c.save();c.rotate(ang,cx,cy);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2,min*.013f));
            p.setColor(i==0?0x99fff1c7:i==1?0x886fdcff:0x77ffffff);
            c.drawOval(cx-radius*pulse,cy-radius*.38f,
                cx+radius*pulse,cy+radius*.38f,p);
            c.restore();
        }
        p.setStyle(Paint.Style.FILL);
        for(int i=0;i<12;i++){
            double angle=i*Math.PI/6+t*.95*(i%2==0?1:-1);
            float rr=radius*(.6f+.35f*(float)Math.sin(t*1.2+i));
            float x=cx+(float)Math.cos(angle)*rr;
            float y=cy+(float)Math.sin(angle)*rr;
            p.setColor(i%3==0?0xeeffe5a4:i%3==1?0xb8ffffff:0xc277e1ff);
            c.drawCircle(x,y,Math.max(1.6f,min*.014f),p);
        }
        if(isAttachedToWindow()&&isShown()&&getWindowVisibility()==View.VISIBLE)postInvalidateDelayed(95);
    }
}
