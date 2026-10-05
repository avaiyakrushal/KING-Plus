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

/** Original KING Plus animated live reactions. All effects render offline. */
public final class LiveEmojiView extends View {
    public static final String[] LABELS={
        "Smile","Laugh","Love","Cry","Wink","Angry","Surprise","Kiss","Cool","Sleep","Celebrate","Blush",
        "Tiger","Lion","Dragon","Crown","Diamond","Rose","Fireworks","Rocket","Unicorn","Panda","Wolf","Cat",
        "Heart Rain","Star Rain","Party Popper","Butterfly","Flame","Lightning","Rainbow","Moon","Sun","Gift","Cake","Music",
        "Clap","Thumbs Up","Victory","Hug","Angel","Devil","Ghost","Skull","Monkey","Bear","Rabbit","Peacock","Swan","Galaxy"
    };
    private static final String[] ICONS={
        "😊","😂","😍","😭","😉","😡","😲","😘","😎","😴","🥳","☺️",
        "🐯","🦁","🐉","👑","💎","🌹","🎆","🚀","🦄","🐼","🐺","🐱",
        "💖","⭐","🎉","🦋","🔥","⚡","🌈","🌙","☀️","🎁","🎂","🎵",
        "👏","👍","✌️","🤗","😇","😈","👻","💀","🐵","🐻","🐰","🦚","🦢","🌌"
    };
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int expression;
    private final long duration;
    private long started;
    private boolean attached;

    public LiveEmojiView(Context context,int expression,long duration){
        super(context);
        this.expression=Math.max(0,Math.min(LABELS.length-1,expression));
        this.duration=duration;
        setContentDescription("Animated "+LABELS[this.expression]);
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
    }
    public static int parse(String token){
        if(token==null||!token.matches("\\[live:(?:[0-9]|[1-4][0-9])\\]"))return -1;
        try{int n=Integer.parseInt(token.substring(6,token.length()-1));return n>=0&&n<LABELS.length?n:-1;}catch(NumberFormatException e){return -1;}
    }
    public static String token(int id){return "[live:"+Math.max(0,Math.min(LABELS.length-1,id))+"]";}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();attached=true;started=SystemClock.uptimeMillis();invalidate();}
    @Override protected void onDetachedFromWindow(){attached=false;super.onDetachedFromWindow();}
    private void color(int c){paint.setShader(null);paint.setStyle(Paint.Style.FILL);paint.setColor(c);}
    private void oval(Canvas c,float l,float t,float r,float b,int color){color(color);c.drawOval(l,t,r,b,paint);}
    private void arc(Canvas c,float l,float t,float r,float b,float start,float sweep,int color,float stroke){color(color);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(stroke);paint.setStrokeCap(Paint.Cap.ROUND);c.drawArc(l,t,r,b,start,sweep,false,paint);paint.setStyle(Paint.Style.FILL);}
    private void heart(Canvas c,float x,float y,float size){color(0xfff34570);Path p=new Path();p.moveTo(x,y+size*.8f);p.cubicTo(x-size*1.3f,y,x-size*.65f,y-size,x,y-size*.35f);p.cubicTo(x+size*.65f,y-size,x+size*1.3f,y,x,y+size*.8f);c.drawPath(p,paint);}

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        long elapsed=SystemClock.uptimeMillis()-started;
        boolean running=duration==0||elapsed<duration;
        float t=running?elapsed/1000f:0f;
        if(expression<12)drawFace(c,t);
        else drawLiveIcon(c,t);
        if(running&&attached&&getWindowVisibility()==VISIBLE&&isShown())postInvalidateDelayed(33);
    }

    private void drawFace(Canvas c,float t){
        float wave=(float)Math.sin(t*6.2);float blink=(t%3.2f>2.95f)?0.12f:1f;
        c.save();float scale=Math.min(getWidth(),getHeight())/100f;c.translate(getWidth()/2f,getHeight()/2f);c.scale(scale,scale);c.translate(0,wave*1.8f);if(expression==1||expression==5)c.rotate(wave*5);c.translate(-50,-50);
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
        c.restore();
    }

    private void drawLiveIcon(Canvas c,float t){
        int w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
        float cx=w*.5f,cy=h*.50f;float min=Math.min(w,h);
        int[] accents={0xffffb300,0xffff5a72,0xff67d6ff,0xffbd73ff,0xffffd84d,0xff70e5a5};
        int accent=accents[(expression-12)%accents.length];
        float pulse=1f+.10f*(float)Math.sin(t*5.2f);
        paint.setShader(new RadialGradient(cx,cy,min*.52f,new int[]{withAlpha(accent,115),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawCircle(cx,cy,min*.52f,paint);paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(2f,min*.025f));paint.setColor(withAlpha(accent,185));
        for(int ring=0;ring<3;ring++){float rr=min*(.24f+.10f*ring)+min*.025f*(float)Math.sin(t*4+ring);c.drawCircle(cx,cy,rr,paint);}paint.setStyle(Paint.Style.FILL);
        c.save();c.translate(cx,cy);c.rotate((float)Math.sin(t*2.8f)*4f);c.scale(pulse,pulse);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(min*.50f);paint.setColor(Color.WHITE);c.drawText(ICONS[expression],0,-(paint.ascent()+paint.descent())/2f,paint);c.restore();
        paint.setTextSize(min*.105f);paint.setTextAlign(Paint.Align.CENTER);paint.setColor(Color.WHITE);paint.setFakeBoldText(true);c.drawText(LABELS[expression],cx,Math.min(h-min*.05f,cy+min*.40f),paint);paint.setFakeBoldText(false);
        String particle=expression==12?"🐾":expression==18?"✨":expression==24?"💖":expression==25?"⭐":expression==28?"🔥":expression==29?"⚡":expression==49?"✦":"•";
        paint.setTextSize(min*.12f);
        for(int i=0;i<10;i++){
            float phase=(t*.22f+i*.103f)%1f;float a=(float)(Math.PI*2*(i/10f)+t*.30f);float rr=min*(.22f+.36f*phase);float x=cx+(float)Math.cos(a)*rr;float y=cy+(float)Math.sin(a)*rr-phase*min*.16f;paint.setAlpha((int)(230*(1f-phase)));paint.setColor(i%2==0?Color.WHITE:accent);c.drawText(particle,x,y,paint);
        }
        paint.setAlpha(255);
    }
    private static int withAlpha(int color,int alpha){return (color&0x00ffffff)|(Math.max(0,Math.min(255,alpha))<<24);}
}
