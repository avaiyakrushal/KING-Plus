package com.kingplus.social;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public final class KingCosmetics {
    private static final String PREFS="kingplus_cosmetics";
    public static final String[] FRAMES={"Minimal Frame","Music Frame","Heart Frame","Royal Frame","Galaxy Frame","Crown Frame"};
    public static final String[] EFFECTS={"None","Welcome Sparkle","Crown Drop","Rose Shower","Galaxy Portal","Royal Arrival"};
    public static final String[] BUBBLES={"Classic Purple","Rose Bubble","Ocean Bubble","Royal Bubble","Galaxy Bubble"};
    public static final String[] BADGES={"None","Music Star","Social Heart","Royal Crown","Galaxy Elite"};
    private KingCosmetics(){}

    private static SharedPreferences p(Context c){return c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);}
    private static String legacy(Context c,String key,String def){
        if(c instanceof Activity){String v=((Activity)c).getPreferences(0).getString(key,"");if(v!=null&&!v.isEmpty())return v;}
        return def;
    }
    public static String frame(Context c){String v=p(c).getString("frame","");if(v==null||v.isEmpty()){v=legacy(c,"equipped_frame","Minimal Frame");p(c).edit().putString("frame",v).apply();}return v;}
    public static String effect(Context c){String v=p(c).getString("effect","");if(v==null||v.isEmpty()){v=legacy(c,"equipped_effect","Welcome Sparkle");p(c).edit().putString("effect",v).apply();}return v;}
    public static String bubble(Context c){String v=p(c).getString("bubble","");if(v==null||v.isEmpty()){v=legacy(c,"equipped_bubble","Classic Purple");p(c).edit().putString("bubble",v).apply();}return v;}
    public static String badge(Context c){String v=p(c).getString("badge","");if(v==null||v.isEmpty()){v=legacy(c,"equipped_badge","None");p(c).edit().putString("badge",v).apply();}return v;}

    public static void setFrame(Context c,String v){p(c).edit().putString("frame",v).apply();if(c instanceof Activity)((Activity)c).getPreferences(0).edit().putString("equipped_frame",v).apply();}
    public static void setEffect(Context c,String v){p(c).edit().putString("effect",v).apply();if(c instanceof Activity)((Activity)c).getPreferences(0).edit().putString("equipped_effect",v).apply();}
    public static void setBubble(Context c,String v){p(c).edit().putString("bubble",v).apply();if(c instanceof Activity)((Activity)c).getPreferences(0).edit().putString("equipped_bubble",v).apply();}
    public static void setBadge(Context c,String v){p(c).edit().putString("badge",v).apply();if(c instanceof Activity)((Activity)c).getPreferences(0).edit().putString("equipped_badge",v).apply();}

    public static boolean unlockedFrame(String name,LevelSystem.Snapshot s){
        if("Minimal Frame".equals(name))return true;if("Music Frame".equals(name))return s.level>=5;if("Heart Frame".equals(name))return s.level>=10;if("Royal Frame".equals(name))return s.vipLevel>=4;if("Galaxy Frame".equals(name))return s.vipLevel>=7;if("Crown Frame".equals(name))return s.vipLevel>=10;return false;
    }
    public static boolean unlockedEffect(String name,LevelSystem.Snapshot s){
        if("None".equals(name))return true;if("Welcome Sparkle".equals(name))return s.level>=3;if("Crown Drop".equals(name))return s.vipLevel>=3;if("Rose Shower".equals(name))return s.vipLevel>=5;if("Galaxy Portal".equals(name))return s.vipLevel>=7;if("Royal Arrival".equals(name))return s.vipLevel>=10;return false;
    }
    public static boolean unlockedBubble(String name,LevelSystem.Snapshot s){
        if("Classic Purple".equals(name))return true;if("Rose Bubble".equals(name))return s.level>=8;if("Ocean Bubble".equals(name))return s.level>=15;if("Royal Bubble".equals(name))return s.vipLevel>=5;if("Galaxy Bubble".equals(name))return s.vipLevel>=8;return false;
    }
    public static boolean unlockedBadge(String name,LevelSystem.Snapshot s){
        if("None".equals(name))return true;if("Music Star".equals(name))return s.level>=10;if("Social Heart".equals(name))return s.level>=20;if("Royal Crown".equals(name))return s.vipLevel>=6;if("Galaxy Elite".equals(name))return s.vipLevel>=9;return false;
    }

    public static String requirementFrame(String name){if("Minimal Frame".equals(name))return "Free";if("Music Frame".equals(name))return "Lv.5";if("Heart Frame".equals(name))return "Lv.10";if("Royal Frame".equals(name))return "VIP 4";if("Galaxy Frame".equals(name))return "VIP 7";if("Crown Frame".equals(name))return "VIP 10";return "Locked";}
    public static String requirementEffect(String name){if("None".equals(name))return "Free";if("Welcome Sparkle".equals(name))return "Lv.3";if("Crown Drop".equals(name))return "VIP 3";if("Rose Shower".equals(name))return "VIP 5";if("Galaxy Portal".equals(name))return "VIP 7";if("Royal Arrival".equals(name))return "VIP 10";return "Locked";}
    public static String requirementBubble(String name){if("Classic Purple".equals(name))return "Free";if("Rose Bubble".equals(name))return "Lv.8";if("Ocean Bubble".equals(name))return "Lv.15";if("Royal Bubble".equals(name))return "VIP 5";if("Galaxy Bubble".equals(name))return "VIP 8";return "Locked";}
    public static String requirementBadge(String name){if("None".equals(name))return "Free";if("Music Star".equals(name))return "Lv.10";if("Social Heart".equals(name))return "Lv.20";if("Royal Crown".equals(name))return "VIP 6";if("Galaxy Elite".equals(name))return "VIP 9";return "Locked";}

    public static String frameEmoji(String name){if("Music Frame".equals(name))return "🎵";if("Heart Frame".equals(name))return "💗";if("Royal Frame".equals(name))return "👑";if("Galaxy Frame".equals(name))return "🌌";if("Crown Frame".equals(name))return "♛";return "◇";}
    public static String effectEmoji(String name){if("Crown Drop".equals(name))return "👑";if("Rose Shower".equals(name))return "🌹";if("Galaxy Portal".equals(name))return "🌌";if("Royal Arrival".equals(name))return "♛";if("Welcome Sparkle".equals(name))return "✨";return "";}
    public static String bubbleEmoji(String name){if("Rose Bubble".equals(name))return "🌹";if("Ocean Bubble".equals(name))return "🌊";if("Royal Bubble".equals(name))return "👑";if("Galaxy Bubble".equals(name))return "🌌";return "💬";}
    public static String badgeEmoji(String name){if("Music Star".equals(name))return "🎵";if("Social Heart".equals(name))return "💗";if("Royal Crown".equals(name))return "👑";if("Galaxy Elite".equals(name))return "🌌";return "";}

    public static int bubbleColor(Context c){
        String n=bubble(c);if("Rose Bubble".equals(n))return 0xffd14d86;if("Ocean Bubble".equals(n))return 0xff287fb8;if("Royal Bubble".equals(n))return 0xff9a6a22;if("Galaxy Bubble".equals(n))return 0xff5e3ea6;return 0xff7146ec;
    }

    public static GradientDrawable avatarFrame(Context c,String name,boolean host){int[] colors=colors(name);GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,colors);d.setShape(GradientDrawable.OVAL);d.setStroke(dp(c,host?4:3),host?0xffffe27a:stroke(name));return d;}
    public static GradientDrawable idFrame(Context c,String name){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,colors(name));d.setCornerRadius(dp(c,14));d.setStroke(dp(c,1),stroke(name));return d;}
    private static int[] colors(String name){if("Music Frame".equals(name))return new int[]{0xff3a6ad8,0xff55c8ff};if("Heart Frame".equals(name))return new int[]{0xffff72a8,0xffffc1d8};if("Royal Frame".equals(name))return new int[]{0xffffc83d,0xffff8c2d};if("Galaxy Frame".equals(name))return new int[]{0xff4b2b83,0xff2864d7,0xffd14cff};if("Crown Frame".equals(name))return new int[]{0xffffd95a,0xffff4fc3,0xff8e4cff};return new int[]{0xffe9e6ef,0xffffffff};}
    private static int stroke(String name){if("Music Frame".equals(name))return 0xff5ed7ff;if("Heart Frame".equals(name))return 0xffff4c91;if("Royal Frame".equals(name))return 0xffffc13a;if("Galaxy Frame".equals(name))return 0xffa363ff;if("Crown Frame".equals(name))return 0xffffd84f;return 0xffa79eaf;}
    private static int dp(Context c,int n){return (int)(n*c.getResources().getDisplayMetrics().density+.5f);}
}
