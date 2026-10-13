package com.kingplus.social;
import java.security.SecureRandom;
public final class KingTttFairRoster9960Test {
 static void yes(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static void no(boolean ok,String why){yes(!ok,why);}
 static SecureRandom scripted(final int x,final int o) {
   return new SecureRandom() {
     int n=0;
     @Override public int nextInt(int bound) {
       int want=n++==0?x:o;
       if(want<0||want>=bound)throw new AssertionError("scripted bound");
       return want;
     }
   };
 }
 public static void main(String[]args){
   yes(KingTttLiveRoster9951.valid("alice",true,"alice",true,true,"alice"),"real TTT Ready");
   no(KingTttLiveRoster9951.valid("alice",true,"alice",false,true,"alice"),"Ready turned OFF");
   no(KingTttLiveRoster9951.valid("alice",true,"alice",null,true,"alice"),"Ready flag absent");
   no(KingTttLiveRoster9951.valid("alice",true,"eve",true,true,"alice"),"forged ready UID");
   no(KingTttLiveRoster9951.valid("alice",true,"alice",true,false,""),"departed ghost");
   int[] first=KingTttFairRoster9960.pickPair(4,scripted(0,0));
   yes(first[0]==0&&first[1]==1,"first X cannot be O");
   int[] last=KingTttFairRoster9960.pickPair(4,scripted(3,2));
   yes(last[0]==3&&last[1]==2,"last X can pair with third");
   int[] later=KingTttFairRoster9960.pickPair(4,scripted(2,2));
   yes(later[0]==2&&later[1]==3,"players beyond alphabetical first pair can play");
   for(int n=2;n<=50;n++)for(int i=0;i<n;i++){
     final int selectedX=i;
     int[] p=KingTttFairRoster9960.pickPair(n,scripted(selectedX,0));
     yes(p[0]!=p[1]&&p[0]>=0&&p[1]<n,"valid distinct selections "+n);
   }
   boolean rejected=false;
   try{KingTttFairRoster9960.pickPair(1,new SecureRandom());}
   catch(IllegalArgumentException ok){rejected=true;}
   yes(rejected,"cannot start with one active ready");
   rejected=false;
   try{KingTttFairRoster9960.pickPair(51,new SecureRandom());}
   catch(IllegalArgumentException ok){rejected=true;}
   yes(rejected,"bound oversized Ready list");
   System.out.println("PASS live TTT Ready false/null, departed ghosts, non-biased distinct X/O slots");
 }
}
