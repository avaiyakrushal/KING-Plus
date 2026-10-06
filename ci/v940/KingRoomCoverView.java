package com.kingplus.social;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Original KING Plus room-card artwork inspired by live-room categories, not copied assets. */
public final class KingRoomCoverView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); private final String category; private final String roomName;
    public KingRoomCoverView(Context c,String cat,String name){super(c);category=cat==null?"Hot":cat;roomName=name==null?"KING Party":name;setContentDescription(category+" room cover");}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();int a=0xff6f294b,b=0xff2b1729;String k=category.toLowerCase(java.util.Locale.US);
        if(k.contains("music")||k.contains("ktv")){a=0xff1f6fb2;b=0xff163152;}
        else if(k.contains("game")){a=0xff5b3db5;b=0xff231f4d;}
        else if(k.contains("date")||k.contains("love")){a=0xffd05a83;b=0xff6d2846;}
        else if(k.contains("event")){a=0xffd17736;b=0xff6c351c;}
        else if(k.contains("family")){a=0xff278f68;b=0xff163f34;}
        LinearGradient g=new LinearGradient(0,0,w,h,a,b,Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);
        p.setColor(0x30ffffff);c.drawCircle(w*.78f,h*.18f,w*.22f,p);c.drawCircle(w*.18f,h*.86f,w*.28f,p);
        p.setColor(0x24ffffff);for(int i=0;i<5;i++)c.drawCircle(w*(.15f+i*.2f),h*(.20f+(i%2)*.18f),w*.06f,p);
        p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.DEFAULT_BOLD);p.setColor(Color.WHITE);
        String icon=k.contains("music")||k.contains("ktv")?"♪":k.contains("game")?"◆":k.contains("date")?"♥":k.contains("event")?"★":k.contains("family")?"♛":"K";
        p.setTextSize(Math.min(w,h)*.42f);c.drawText(icon,w/2f,h*.58f,p);
        p.setTextSize(Math.min(w,h)*.085f);p.setColor(0xddffffff);String n=roomName.length()>16?roomName.substring(0,16):roomName;c.drawText(n,w/2f,h*.82f,p);
    }
}
