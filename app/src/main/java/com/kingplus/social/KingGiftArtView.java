package com.kingplus.social;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.View;
import android.graphics.Camera;
import android.graphics.Matrix;
import java.util.Locale;

/** 88 distinct contextual gift pictures, not the previous rainbow spheres.
 * Emoji art uses Android's installed multicolor emoji font; animated pedestal,
 * depth tilt, light rays and particles are original native Canvas shapes.
 * Visible-only 8fps for shop tiles; 20fps for selected hero to save RAM.
 */
public final class KingGiftArtView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Camera camera=new Camera();
    private final Matrix rotation=new Matrix();
    private final Rect visible=new Rect();
    private int index;
    private String name;
    private String icon;
    private boolean hero;
    private long started=SystemClock.uptimeMillis();
    private static final int[] ACCENTS={
      0xffff6fba,0xffffcc74,0xff61cbff,0xffaf83ff,
      0xff78f3cb,0xffff9668,0xffffd36a,0xff89a7ff
    };
    public KingGiftArtView(Context c,int i,String n){
        this(c,i,n,defaultIcon(n));
    }
    public KingGiftArtView(Context c,int i,String n,String symbol){
        super(c);setGift(i,n,symbol);
        setLayerType(View.LAYER_TYPE_NONE,null); // No cached layers per tile; low-RAM safe.
    }
    public void setShowcaseMode(boolean value){
        hero=value;
        setLayerType(View.LAYER_TYPE_NONE,null);
        invalidate();
    }
    public void setGift(int i,String n,String symbol){
        index=Math.max(0,i);
        name=(n==null||n.isEmpty())?"Gift":n;
        icon=(symbol==null||symbol.trim().isEmpty())?defaultIcon(name):symbol;
        // Semantic overrides where the generic source symbol obscures gift identity.
        String lower=name.toLowerCase(Locale.US);
        if(lower.contains("flying kiss")||lower.contains("kiss sound")||
           lower.contains("kiss show")||lower.contains("kiss kingdom"))icon="💋";
        else if(lower.contains("super car")||lower.contains("royal car")||
                lower.contains("dream car"))icon="🏎️";
        else if(lower.contains("galaxy ship"))icon="🛸";
        else if(lower.contains("moon palace"))icon="🏰";
        else if(lower.contains("gold rose")||lower.contains("rose rain"))icon="🌹";
        else if(lower.contains("royal crown"))icon="👑";
        else if(lower.contains("gift night")||lower.equals("vip night"))icon="🌃";
        else if(lower.contains("firework"))icon="🎆";
        else if(lower.contains("private jet"))icon="✈️";
        else if(lower.contains("rocket"))icon="🚀";
        else if(lower.contains("disco"))icon="🪩";
        setContentDescription("Animated 3D-style "+name+" gift");
        started=SystemClock.uptimeMillis();
        invalidate();
    }
    private static String defaultIcon(String n){
        String name=n==null?"":n.toLowerCase(Locale.US);
        if(name.contains("rose"))return "🌹";
        if(name.contains("kiss"))return "💋";
        if(name.contains("love")||name.contains("heart"))return "💖";
        if(name.contains("ring"))return "💍";
        if(name.contains("helicopter"))return "🚁";
        if(name.contains("rocket"))return "🚀";
        if(name.contains("car"))return "🏎️";
        if(name.contains("plane")||name.contains("jet"))return "✈️";
        if(name.contains("yacht"))return "🛥️";
        if(name.contains("castle")||name.contains("palace"))return "🏰";
        if(name.contains("panda"))return "🐼";
        if(name.contains("crown"))return "👑";
        if(name.contains("teapot"))return "🫖";
        if(name.contains("horse"))return "🐎";
        if(name.contains("eagle"))return "🦅";
        if(name.contains("dragon"))return "🐉";
        if(name.contains("night"))return "🌃";
        if(name.contains("rose"))return "🌹";
        if(name.contains("birthday")||name.contains("cake"))return "🎂";
        return "🎁";
    }
    private int accent(){return ACCENTS[index%ACCENTS.length];}
    private int col(int color,int a){
        return (color&0x00ffffff)|((Math.min(255,Math.max(0,a)))<<24);
    }
    private void spot(Canvas c,float cx,float cy,float r,int tint){
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(cx,cy,Math.max(3,r),
            new int[]{col(tint,96),col(tint,28),0},
            new float[]{0,.46f,1},Shader.TileMode.CLAMP));
        c.drawCircle(cx,cy,r,p);p.setShader(null);
    }
    private void star(Canvas c,float x,float y,float r,int tint,int alpha){
        p.setShader(null);p.setColor(col(tint,alpha));
        p.setStrokeWidth(Math.max(.8f,r*.13f));p.setStyle(Paint.Style.STROKE);
        c.drawLine(x-r,y,x+r,y,p);c.drawLine(x,y-r,x,y+r,p);
        p.setStyle(Paint.Style.FILL);
        c.drawCircle(x,y,Math.max(.7f,r*.11f),p);
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        final float w=getWidth(),h=getHeight();
        if(w<8||h<8)return;
        final float min=Math.min(w,h),time=(SystemClock.uptimeMillis()-started)/1000f;
        final float cx=w*.5f,cy=h*(hero?.42f:.48f);
        final float bob=(float)Math.sin(time*(hero?2.6f:1.9f))*min*.045f;
        final int tint=accent();
        // Stage has an elliptical footprint, never a sphere masquerading as a gift.
        spot(c,cx,cy,min*.65f,tint);
        p.setColor(0x6608031e);p.setStyle(Paint.Style.FILL);
        c.drawOval(cx-min*.40f,cy+min*.28f,
                   cx+min*.40f,cy+min*.40f,p);
        p.setShader(new LinearGradient(cx,cy+min*.27f,cx,cy+min*.41f,
            new int[]{col(tint,170),0x29140b35,0x5b070516},
            null,Shader.TileMode.CLAMP));
        c.drawOval(cx-min*.39f,cy+min*.25f,
                   cx+min*.39f,cy+min*.38f,p);
        p.setShader(null);
        p.setColor(col(0xffffeab0,135));p.setStrokeWidth(Math.max(1,min*.012f));
        p.setStyle(Paint.Style.STROKE);
        c.drawOval(cx-min*.40f,cy+min*.25f,
                   cx+min*.40f,cy+min*.39f,p);
        p.setStyle(Paint.Style.FILL);

        // Animated colorful gift silhouettes are unique by icon/name:
        // flowers, vehicles, rockets, kisses and buildings remain recognizable.
        // A small Y-axis perspective wobble adds depth without full 3D engine.
        float iconSize=min*(hero?.64f:.69f);
        p.setShader(null);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(iconSize);
        float baseline=-(p.ascent()+p.descent())/2f;
        c.save();
        c.translate(cx,cy+bob);
        camera.save();
        camera.rotateY((float)Math.sin(time*1.3f)*(hero?11f:5f));
        camera.rotateX((float)Math.sin(time*.9f)*(hero?6f:3f));
        rotation.reset();camera.getMatrix(rotation);camera.restore();
        c.concat(rotation);
        c.rotate((float)Math.sin(time*1.55f)*(hero?5f:2f));
        // Soft deep shadow and the bright main colored glyph.
        p.setColor(col(tint,110));
        c.drawText(icon,iconSize*.035f,baseline+iconSize*.065f,p);
        p.setColor(Color.WHITE);
        c.drawText(icon,0,baseline,p);
        c.restore();

        int starCount=hero?13:5;
        for(int i=0;i<starCount;i++){
            double ang=i*2.39996+time*(hero?.6f:.36f);
            float phase=(time*.17f+i*.18f)%1f;
            float r=min*(.17f+.31f*phase);
            float x=cx+(float)Math.cos(ang)*r;
            float y=cy+(float)Math.sin(ang)*r*.72f;
            star(c,x,y,Math.max(1,min*(hero?.018f:.014f)),i%2==0?0xffffefc6:tint,
                 (int)(195*(1-phase)));
        }
        // Hero panel adds name, not tiny duplicated text on every miniature tile.
        if(hero){
            p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextSize(Math.max(13,w*.09f));p.setColor(0xffffefcc);
            String label=name.length()>21?name.substring(0,20)+"…":name;
            c.drawText(label,cx,h*.91f,p);
        }
        // Only the visible cells animate. No hidden full-screen software surfaces.
        if(isAttachedToWindow()&&isShown()&&getWindowVisibility()==View.VISIBLE
                &&getGlobalVisibleRect(visible)){
            postInvalidateDelayed(hero?52:150);
        }
    }
}
