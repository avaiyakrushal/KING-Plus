package com.kingplus.social;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.Random;

/** Original KING Plus login backdrop. */
public final class KingLoginBackdropView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] stars=new float[72];
    public KingLoginBackdropView(Context c){super(c);Random r=new Random(940);for(int i=0;i<24;i++){stars[i*3]=r.nextFloat();stars[i*3+1]=r.nextFloat();stars[i*3+2]=.6f+r.nextFloat()*1.6f;}}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();p.setShader(new LinearGradient(0,0,w,h,new int[]{0xff08162d,0xff241146,0xff5a245c,0xff140c2a},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
        p.setShader(new RadialGradient(w*.52f,h*.24f,w*.55f,0x99a050ff,0x00000000,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h*.65f,p);p.setShader(null);
        for(int i=0;i<24;i++){p.setColor(i%3==0?0xaaffdf70:0x66ffffff);c.drawCircle(stars[i*3]*w,stars[i*3+1]*h,stars[i*3+2]*2.2f,p);}
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2f,w*.006f));p.setColor(0x22ffffff);for(int i=0;i<4;i++)c.drawCircle(w*.5f,h*.27f,w*(.22f+i*.08f),p);p.setStyle(Paint.Style.FILL);
        p.setColor(0x25ffffff);Path stage=new Path();stage.moveTo(0,h*.60f);stage.quadTo(w*.22f,h*.48f,w*.42f,h*.62f);stage.quadTo(w*.65f,h*.76f,w,h*.57f);stage.lineTo(w,h);stage.lineTo(0,h);stage.close();c.drawPath(stage,p);
        float cx=w*.5f,cy=h*.22f,r=w*.12f;p.setColor(0xffffd968);Path crown=new Path();crown.moveTo(cx-r,cy+r*.45f);crown.lineTo(cx-r*.78f,cy-r*.48f);crown.lineTo(cx-r*.26f,cy-r*.02f);crown.lineTo(cx,cy-r*.78f);crown.lineTo(cx+r*.26f,cy-r*.02f);crown.lineTo(cx+r*.78f,cy-r*.48f);crown.lineTo(cx+r,cy+r*.45f);crown.close();c.drawPath(crown,p);p.setColor(0x88ffffff);c.drawRoundRect(cx-r*.8f,cy+r*.26f,cx+r*.8f,cy+r*.50f,10,10,p);
    }
}
