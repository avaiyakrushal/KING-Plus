package com.kingplus.social;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

public class KingDrawPadView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    public KingDrawPadView(Context c){super(c);paint.setColor(Color.WHITE);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(5));paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);setBackgroundColor(0xff2a173f);}
    private float dp(int n){return n*getResources().getDisplayMetrics().density;}
    public void clear(){path.reset();invalidate();}
    @Override protected void onDraw(Canvas c){super.onDraw(c);c.drawPath(path,paint);}
    @Override public boolean onTouchEvent(MotionEvent e){float x=e.getX(),y=e.getY();if(e.getAction()==MotionEvent.ACTION_DOWN){path.moveTo(x,y);invalidate();return true;}if(e.getAction()==MotionEvent.ACTION_MOVE){path.lineTo(x,y);invalidate();return true;}return e.getAction()==MotionEvent.ACTION_UP||super.onTouchEvent(e);}
}
