package com.kingplus.social;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.View;

/** Offline animated expressions; no remote animation download is required. */
public final class LiveEmojiView extends View {
    public static final String[] LABELS={"Smile","Laugh","Love","Cry","Wink","Angry","Surprise","Kiss","Cool","Sleep","Celebrate","Blush"};
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int expression;
    private final long duration;
    private long started;
    private boolean attached;
    public LiveEmojiView(Context context,int expression,long duration){super(context);this.expression=Math.max(0,Math.min(11,expression));this.duration=duration;setContentDescription("Animated "+LABELS[this.expression]);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    public static int parse(String token){if(token==null||!token.matches("\\[live:(?:[0-9]|1[01])\\]"))return -1;try{return Integer.parseInt(token.substring(6,token.length()-1));}catch(NumberFormatException e){return -1;}}
    public static String token(int id){return "[live:"+id+"]";}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();attached=true;started=SystemClock.uptimeMillis();invalidate();}
    @Override protected void onDetachedFromWindow(){attached=false;super.onDetachedFromWindow();}
    private void color(int c){paint.setShader(null);paint.setStyle(Paint.Style.FILL);paint.setColor(c);}
    private void oval(Canvas c,float l,float t,float r,float b,int color){color(color);c.drawOval(l,t,r,b,paint);}
    private void arc(Canvas c,float l,float t,float r,float b,float start,float sweep,int color,float stroke){color(color);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(stroke);paint.setStrokeCap(Paint.Cap.ROUND);c.drawArc(l,t,r,b,start,sweep,false,paint);paint.setStyle(Paint.Style.FILL);}
    private void heart(Canvas c,float x,float y,float size){color(0xfff34570);Path p=new Path();p.moveTo(x,y+size*.8f);p.cubicTo(x-size*1.3f,y,x-size*.65f,y-size,x,y-size*.35f);p.cubicTo(x+size*.65f,y-size,x+size*1.3f,y,x,y+size*.8f);c.drawPath(p,paint);}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);long elapsed=SystemClock.uptimeMillis()-started;boolean running=duration==0||elapsed<duration;float t=running?elapsed/1000f:0f;float wave=(float)Math.sin(t*6.2);float blink=(t%3.2f>2.95f)?0.12f:1f;
        c.save();float scale=Math.min(getWidth(),getHeight())/100f;c.translate(getWidth()/2f,getHeight()/2f);c.scale(scale,scale);c.translate(0,running?wave*1.8f:0);if(expression==1||expression==5)c.rotate(wave*5);c.translate(-50,-50);
        paint.setStyle(Paint.Style.FILL);paint.setShader(new RadialGradient(35,28,66,new int[]{0xfffff3a0,0xffffce3c,0xffe89408},null,Shader.TileMode.CLAMP));c.drawCircle(50,49,37,paint);paint.setShader(null);
        int dark=0xff65400c;float eyeH=6*blink;
        if(expression==2){heart(c,36,40,10);heart(c,64,40,10);}
        else if(expression==8){oval(c,23,32,47,48,0xff252635);oval(c,53,32,77,48,0xff252635);color(dark);paint.setStrokeWidth(4);c.drawLine(44,37,56,37,paint);}
        else if(expression==9||expression==1){arc(c,26,33,44,44,190,160,dark,3);arc(c,56,33,74,44,190,160,dark,3);}
        else{oval(c,31,39-eyeH,41,39+eyeH,dark);if(expression==4)arc(c,56,33,73,44,10,160,dark,3);else oval(c,59,39-eyeH,69,39+eyeH,dark);}
        if(expression==5){color(dark);paint.setStrokeWidth(4);c.drawLine(27,25,43,30,paint);c.drawLine(57,30,73,25,paint);arc(c,36,56,64,72,195,150,dark,3);}
        else if(expression==6||expression==9)oval(c,43,56,57,73+wave*2,dark);
        else if(expression==7){heart(c,51,63,7);float f=(t%1.5f)/1.5f;heart(c,69+f*13,51-f*20,4+f*4);}
        else if(expression==3){arc(c,34,60,66,76,195,150,dark,3);float drop=(t%0.8f)/.8f;oval(c,27,45+drop*22,34,55+drop*22,0xff54cafa);oval(c,66,45+((drop+.5f)%1)*22,73,55+((drop+.5f)%1)*22,0xff54cafa);}
        else if(expression==1){oval(c,31,52,69,76+wave*2,dark);oval(c,38,66,63,77+wave*2,0xfff47577);color(Color.WHITE);c.drawRoundRect(35,53,65,59,2,2,paint);}
        else arc(c,32,47,68,71,8,165,dark,3.5f);
        if(expression==11||expression==0){oval(c,22,49,36,55,0x80fa6c65);oval(c,64,49,78,55,0x80fa6c65);}
        if(expression==10){for(int i=0;i<9;i++){float f=(t*.6f+i*.12f)%1;color(i%2==0?0xfffa61a7:0xff57caff);c.drawRect(7+i*10,5+f*85,10+i*10,10+f*85,paint);}}
        if(expression==9){color(0xff727ad5);paint.setTextSize(14);paint.setFakeBoldText(true);c.drawText("Z",75,25-wave*4,paint);paint.setFakeBoldText(false);}
        if(expression==2){float f=(t%1.7f)/1.7f;heart(c,14,45-f*28,3+f*3);heart(c,86,51-f*32,4+f*2);}
        c.restore();if(running&&attached&&getWindowVisibility()==VISIBLE&&isShown())postInvalidateDelayed(33);
    }
}
