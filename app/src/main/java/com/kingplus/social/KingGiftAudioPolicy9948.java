package com.kingplus.social;
import java.util.Locale;
/** KING-original identifiable sonic styles by Gift name, no third-party audio. */
public final class KingGiftAudioPolicy9948 {
 private KingGiftAudioPolicy9948(){}
 public static String style(String gift){
   String s=gift==null?"":gift.toLowerCase(Locale.US);
   if(s.contains("vip night")||s.contains("vip-night"))return "vipnight";
   if(s.contains("kiss"))return "kiss";
   if(s.contains("rose")||s.contains("flower")||s.contains("bouquet"))return "rose";
   if(s.contains("heart")||s.contains("hug")||s.contains("love"))return "love";
   if(s.contains("rocket")||s.contains("jet")||s.contains("plane")||s.contains("helicopter")||s.contains("spaceship"))return "flight";
   if(s.contains("car")||s.contains("bike")||s.contains("bus")||s.contains("supercar"))return "drive";
   if(s.contains("firework")||s.contains("explosion")||s.contains("volcano"))return "firework";
   if(s.contains("panda")||s.contains("horse")||s.contains("eagle")||s.contains("dragon")||s.contains("unicorn"))return "animal";
   if(s.contains("vip")||s.contains("night")||s.contains("king")||s.contains("royal")
      ||s.contains("throne")||s.contains("crown")||s.contains("palace")||s.contains("castle"))return "royal";
   if(s.contains("disco")||s.contains("dance")||s.contains("music")||s.contains("party"))return "music";
   if(s.contains("diamond")||s.contains("crystal")||s.contains("ring"))return "crystal";
   return "magic";
 }
 public static int signature(String gift){
   String s=gift==null?"":gift.trim().toLowerCase(Locale.US);
   int hash=0x187f;
   for(int i=0;i<s.length();i++)hash=31*hash+s.charAt(i);
   return hash&0x7fffffff;
 }
}
