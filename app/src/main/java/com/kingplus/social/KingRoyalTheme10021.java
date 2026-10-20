package com.kingplus.social;

import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.content.Context;

/** Premium gold, cream and violet visual identity shared by KING Plus screens. */
public final class KingRoyalTheme10021 {
    public static final int INK=0xff292044;
    public static final int CREAM=0xfffff8eb;
    public static final int GOLD=0xffdcb65d;
    public static final int VIOLET=0xff7555b7;
    public static final int SUBTITLE=0xff766a80;
    private KingRoyalTheme10021(){}
    private static float dp(Context c,float value){return c.getResources().getDisplayMetrics().density*value;}
    public static GradientDrawable page(){return new GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xfffff6db,0xfffaf1fd,0xfffffaf1});}
    public static GradientDrawable header(Context c){GradientDrawable d=new GradientDrawable(
        GradientDrawable.Orientation.TL_BR,new int[]{0xffffedb9,0xfff5e5ff});
        d.setCornerRadius(dp(c,18)); d.setStroke((int)dp(c,1),0xffecd28b);return d;}
    public static GradientDrawable card(Context c,int radius){GradientDrawable d=new GradientDrawable(
        GradientDrawable.Orientation.TL_BR,new int[]{0xffffffff,0xfffffaef});
        d.setCornerRadius(dp(c,radius));d.setStroke((int)Math.max(1,dp(c,1)),0xffe5c876);return d;}
    public static GradientDrawable violetCard(Context c,int radius){GradientDrawable d=new GradientDrawable(
        GradientDrawable.Orientation.TL_BR,new int[]{0xff6242a4,0xff9870d2});
        d.setCornerRadius(dp(c,radius));d.setStroke((int)Math.max(1,dp(c,1)),0xfff0cb68);return d;}
    public static GradientDrawable bottomNav(Context c){GradientDrawable d=new GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xfffffaed,0xffffffff});
        d.setStroke((int)Math.max(1,dp(c,1)),0xfff2dfad);return d;}
}
