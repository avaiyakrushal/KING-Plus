package com.kingplus.social;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Original KING Plus gift illustration. No third-party gift artwork is bundled. */
public final class KingGiftArtView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int index; private final String name;
    public KingGiftArtView(Context c,int i,String n){super(c);index=i;name=n==null?"Gift":n;setContentDescription(name);}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);float w=getWidth(),h=getHeight(),cx=w/2f,cy=h/2f,r=Math.min(w,h)*.34f;
        int[] palette={0xffff657a,0xffffd04a,0xff66d8ff,0xffa879ff,0xff61d69b,0xffff9c52,0xfff06fd4,0xff8bd46b};
        int col=palette[Math.abs(index)%palette.length];p.setStyle(Paint.Style.FILL);p.setColor((col&0x00ffffff)|0x26000000);c.drawCircle(cx,cy,r*1.35f,p);
        p.setColor(col);int type=Math.abs(index)%8;
        if(type==0){ // flower
            for(int k=0;k<6;k++){double a=Math.PI*2*k/6;float x=cx+(float)Math.cos(a)*r*.58f,y=cy+(float)Math.sin(a)*r*.58f;c.drawCircle(x,y,r*.34f,p);}p.setColor(0xffffec84);c.drawCircle(cx,cy,r*.34f,p);
        } else if(type==1){ // heart
            Path q=new Path();q.moveTo(cx,cy+r*.78f);q.cubicTo(cx-r*1.3f,cy, cx-r*.82f,cy-r*.88f,cx,cy-r*.3f);q.cubicTo(cx+r*.82f,cy-r*.88f,cx+r*1.3f,cy,cx,cy+r*.78f);c.drawPath(q,p);
        } else if(type==2){ // crown
            Path q=new Path();q.moveTo(cx-r,cy+r*.65f);q.lineTo(cx-r*.8f,cy-r*.45f);q.lineTo(cx-r*.3f,cy);q.lineTo(cx,cy-r*.8f);q.lineTo(cx+r*.3f,cy);q.lineTo(cx+r*.8f,cy-r*.45f);q.lineTo(cx+r,cy+r*.65f);q.close();c.drawPath(q,p);p.setColor(0xfffff0a8);c.drawRect(cx-r*.82f,cy+r*.43f,cx+r*.82f,cy+r*.66f,p);
        } else if(type==3){ // star
            Path q=new Path();for(int k=0;k<10;k++){double a=-Math.PI/2+k*Math.PI/5;float rr=(k%2==0?r:r*.45f);float x=cx+(float)Math.cos(a)*rr,y=cy+(float)Math.sin(a)*rr;if(k==0)q.moveTo(x,y);else q.lineTo(x,y);}q.close();c.drawPath(q,p);
        } else if(type==4){ // gem
            Path q=new Path();q.moveTo(cx,cy-r);q.lineTo(cx+r,cy-r*.25f);q.lineTo(cx+r*.62f,cy+r);q.lineTo(cx-r*.62f,cy+r);q.lineTo(cx-r,cy-r*.25f);q.close();c.drawPath(q,p);p.setColor(0x88ffffff);c.drawLine(cx-r*.75f,cy-r*.18f,cx+r*.75f,cy-r*.18f,p);c.drawLine(cx,cy-r*.9f,cx,cy+r*.9f,p);
        } else if(type==5){ // rocket
            Path q=new Path();q.moveTo(cx,cy-r);q.cubicTo(cx+r*.62f,cy-r*.28f,cx+r*.5f,cy+r*.35f,cx,cy+r*.55f);q.cubicTo(cx-r*.5f,cy+r*.35f,cx-r*.62f,cy-r*.28f,cx,cy-r);q.close();c.drawPath(q,p);p.setColor(Color.WHITE);c.drawCircle(cx,cy-r*.18f,r*.2f,p);p.setColor(0xffff8a45);c.drawCircle(cx,cy+r*.76f,r*.22f,p);
        } else if(type==6){ // castle
            c.drawRect(cx-r*.8f,cy-r*.15f,cx+r*.8f,cy+r*.75f,p);c.drawRect(cx-r,cy-r*.7f,cx-r*.45f,cy+r*.75f,p);c.drawRect(cx+r*.45f,cy-r*.7f,cx+r,cy+r*.75f,p);p.setColor(0x99ffffff);c.drawRect(cx-r*.18f,cy+r*.18f,cx+r*.18f,cy+r*.75f,p);
        } else { // trophy
            c.drawRect(cx-r*.18f,cy-r*.05f,cx+r*.18f,cy+r*.62f,p);c.drawOval(cx-r*.62f,cy-r*.75f,cx+r*.62f,cy+r*.15f,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2f,r*.12f));c.drawArc(cx-r,cy-r*.52f,cx-r*.3f,cy+r*.18f,80,200,false,p);c.drawArc(cx+r*.3f,cy-r*.52f,cx+r,cy+r*.18f,-100,200,false,p);p.setStyle(Paint.Style.FILL);c.drawRect(cx-r*.5f,cy+r*.62f,cx+r*.5f,cy+r*.82f,p);
        }
        p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(Math.min(w,h)*.13f);p.setColor(0xddffffff);String label=name.length()>7?name.substring(0,7):name;c.drawText(label,cx,h-2,p);
    }
}
