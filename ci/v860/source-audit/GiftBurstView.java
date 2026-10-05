package com.kingplus.social;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.Random;

/** Original KING Plus full-screen gift animation. */
public class GiftBurstView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String icon;
    private final String title;
    private final String subtitle;
    private final int accent;
    private final float[] px = new float[28];
    private final float[] py = new float[28];
    private final float[] ps = new float[28];
    private float progress;
    private Runnable endAction;
    private ValueAnimator animator;

    public GiftBurstView(Context context, String icon, String title, String subtitle, int accent) {
        super(context);
        this.icon = icon == null || icon.isEmpty() ? "🎁" : icon;
        this.title = title == null ? "Gift" : title;
        this.subtitle = subtitle == null ? "" : subtitle;
        this.accent = accent;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        Random r = new Random((this.title + this.subtitle).hashCode());
        for (int i = 0; i < px.length; i++) {
            px[i] = r.nextFloat();
            py[i] = r.nextFloat();
            ps[i] = .55f + r.nextFloat() * 1.25f;
        }
    }

    public void start(Runnable endAction, long durationMs) {
        this.endAction = endAction;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(Math.max(1800, durationMs));
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> { progress = (float)a.getAnimatedValue(); invalidate(); });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                if (GiftBurstView.this.endAction != null) GiftBurstView.this.endAction.run();
            }
        });
        animator.start();
    }

    @Override protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
        super.onDetachedFromWindow();
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        float fadeIn = Math.min(1f, progress / .16f);
        float fadeOut = progress > .78f ? Math.max(0f, (1f-progress)/.22f) : 1f;
        float alpha = fadeIn * fadeOut;
        float cx = w * .5f, cy = h * .44f;

        int baseA = (int)(145 * alpha);
        glow.setShader(new RadialGradient(cx, cy, Math.max(w,h)*.52f,
                new int[]{withAlpha(accent, baseA), Color.TRANSPARENT}, null, Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, Math.max(w,h)*.52f, glow);
        glow.setShader(null);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        paint.setAlpha((int)(255*alpha));
        float pop = progress < .28f ? .55f + (progress/.28f)*.55f : 1.10f - Math.min(.10f,(progress-.28f)*.12f);
        paint.setTextSize(Math.min(w,h) * .19f * pop);
        c.drawText(icon, cx, cy, paint);

        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        paint.setTextSize(Math.max(30f, w*.055f));
        c.drawText(title, cx, cy + Math.min(w,h)*.13f, paint);
        paint.setTypeface(android.graphics.Typeface.DEFAULT);
        paint.setTextSize(Math.max(20f, w*.036f));
        paint.setColor(0xfffff0b0);
        c.drawText(subtitle, cx, cy + Math.min(w,h)*.19f, paint);

        paint.setColor(Color.WHITE);
        for (int i=0;i<px.length;i++) {
            float angle = (float)(Math.PI*2*px[i]);
            float dist = (Math.min(w,h)*.10f) + progress * (Math.min(w,h)*.45f) * ps[i];
            float x = cx + (float)Math.cos(angle)*dist;
            float y = cy + (float)Math.sin(angle)*dist - progress*h*.08f + (py[i]-.5f)*h*.08f;
            paint.setAlpha((int)(210*alpha*(1f-progress*.45f)));
            float r = 2.5f + 7f*ps[i]*(1f-progress*.35f);
            c.drawCircle(x,y,r,paint);
        }
        paint.setAlpha(255);
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00ffffff) | (Math.max(0,Math.min(255,alpha)) << 24);
    }
}
