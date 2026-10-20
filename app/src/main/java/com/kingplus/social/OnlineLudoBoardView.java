package com.kingplus.social;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import java.util.List;
import java.util.Map;

/** Read-only board projection. Moves use the numbered accessible buttons below. */
public final class OnlineLudoBoardView extends View {
    private static final int[] COLORS={0xffff5b67,0xff4aa8ff,0xffffc646,0xff52cf76};
    // Clockwise path; offsets 0/13/26/39 match the server's capture calculation.
    private static final int[][] TRACK={
        {1,6},{2,6},{3,6},{4,6},{5,6},{6,5},{6,4},{6,3},{6,2},{6,1},{6,0},{7,0},{8,0},
        {8,1},{8,2},{8,3},{8,4},{8,5},{9,6},{10,6},{11,6},{12,6},{13,6},{14,6},{14,7},{14,8},
        {13,8},{12,8},{11,8},{10,8},{9,8},{8,9},{8,10},{8,11},{8,12},{8,13},{8,14},{7,14},{6,14},
        {6,13},{6,12},{6,11},{6,10},{6,9},{5,8},{4,8},{3,8},{2,8},{1,8},{0,8},{0,7},{0,6}};
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private Map<String,Object> state;
    public OnlineLudoBoardView(Context context){super(context);setContentDescription("Ludo board. Use numbered piece buttons below to move.");}
    public void setState(Map<String,Object> value){state=value;invalidate();}
    @Override protected void onMeasure(int w,int h){int size=MeasureSpec.getSize(w);setMeasuredDimension(size,size);}
    private int n(Object v,int fallback){return v instanceof Number?((Number)v).intValue():fallback;}
    private void rect(Canvas c,float x,float y,float w,float h,int color){paint.setColor(color);paint.setStyle(Paint.Style.FILL);c.drawRect(x,y,x+w,y+h,paint);}
    private void text(Canvas c,String value,float x,float y,float size,int color){paint.setColor(color);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(size);paint.setStyle(Paint.Style.FILL);c.drawText(value,x,y-(paint.ascent()+paint.descent())/2,paint);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float u=getWidth()/15f;rect(c,0,0,getWidth(),getHeight(),0xffeee9f6);
        int[][] corners={{0,0},{9,0},{9,9},{0,9}};
        for(int seat=0;seat<4;seat++){int x=corners[seat][0],y=corners[seat][1];rect(c,x*u,y*u,6*u,6*u,COLORS[seat]);rect(c,(x+1)*u,(y+1)*u,4*u,4*u,0xfffbf8ff);}
        for(int square=0;square<52;square++){int[] p=TRACK[square];int color=square%13==0?COLORS[square/13]:Color.WHITE;rect(c,p[0]*u,p[1]*u,u,u,color);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(u*.025f);paint.setColor(0xffb9adc8);c.drawRect(p[0]*u,p[1]*u,(p[0]+1)*u,(p[1]+1)*u,paint);if(square%13==8||square%13==0)text(c,"★",(p[0]+.5f)*u,(p[1]+.5f)*u,u*.65f,0xff776381);}
        for(int seat=0;seat<4;seat++)for(int k=0;k<5;k++){float[] p=home(seat,k);rect(c,p[0]*u,p[1]*u,u,u,COLORS[seat]);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(u*.025f);paint.setColor(0xffeee9f6);c.drawRect(p[0]*u,p[1]*u,(p[0]+1)*u,(p[1]+1)*u,paint);}
        int[][] edges={{6,6,6,9},{6,6,9,6},{9,6,9,9},{6,9,9,9}};
        for(int seat=0;seat<4;seat++){Path p=new Path();p.moveTo(7.5f*u,7.5f*u);p.lineTo(edges[seat][0]*u,edges[seat][1]*u);p.lineTo(edges[seat][2]*u,edges[seat][3]*u);p.close();paint.setColor(COLORS[seat]);paint.setStyle(Paint.Style.FILL);c.drawPath(p,paint);}
        if(state==null)return;Object raw=state.get("pieces");if(!(raw instanceof List))return;List<?> pieces=(List<?>)raw;int capacity=n(state.get("capacity"),4),turn=n(state.get("turn"),0);
        for(int seat=0;seat<4;seat++){if(capacity==2&&(seat==1||seat==3))continue;for(int k=0;k<4;k++){int index=seat*4+k,pos=index<pieces.size()?n(pieces.get(index),-1):-1;float x,y;
            if(pos<0){x=corners[seat][0]+2f+(k%2)*2;y=corners[seat][1]+2f+(k/2)*2;}
            else if(pos>=56){x=6.85f+(k%2)*1.3f;y=6.85f+(k/2)*1.3f;}
            else {float[] p=pos<=50?new float[]{TRACK[(seat*13+pos)%52][0],TRACK[(seat*13+pos)%52][1]}:home(seat,pos-51);x=p[0]+.5f;y=p[1]+.5f;int stacked=0;for(int j=0;j<4;j++)if(seat*4+j<pieces.size()&&n(pieces.get(seat*4+j),-1)==pos)stacked++;if(stacked>1){x+=(k%2==0?-.19f:.19f);y+=(k/2==0?-.19f:.19f);}}
            paint.setStyle(Paint.Style.FILL);paint.setColor(COLORS[seat]);c.drawCircle(x*u,y*u,u*.32f,paint);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(u*.065f);paint.setColor(seat==turn?Color.WHITE:0xff463653);c.drawCircle(x*u,y*u,u*.32f,paint);text(c,String.valueOf(k+1),x*u,y*u,u*.39f,Color.BLACK);
        }}
    }
    private float[] home(int seat,int k){switch(seat){case 0:return new float[]{1+k,7};case 1:return new float[]{7,1+k};case 2:return new float[]{13-k,7};default:return new float[]{7,13-k};}}
}
