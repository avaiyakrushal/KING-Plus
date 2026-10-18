package com.kingplus.social;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

/** Simple on-device drawing surface used by Draw & Guess. */
public final class KingDrawPadView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    public KingDrawPadView(Context context) {
        super(context);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(5));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        setBackgroundColor(0xff2a173f);
    }
    private float dp(float value) { return value * getResources().getDisplayMetrics().density; }
    public void clear() { path.reset(); invalidate(); }
    @Override protected void onDraw(Canvas canvas) { super.onDraw(canvas); canvas.drawPath(path, paint); }
    @Override public boolean onTouchEvent(MotionEvent event) {
        float x=event.getX(), y=event.getY();
        if(event.getAction()==MotionEvent.ACTION_DOWN){path.moveTo(x,y);invalidate();return true;}
        if(event.getAction()==MotionEvent.ACTION_MOVE){path.lineTo(x,y);invalidate();return true;}
        return event.getAction()==MotionEvent.ACTION_UP || super.onTouchEvent(event);
    }
}
