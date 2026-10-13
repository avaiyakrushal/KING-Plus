package com.kingplus.social;
public final class KingLiveDrawRound9967Test {
 static void yes(boolean x,String reason){if(!x)throw new AssertionError(reason);}
 static void no(boolean x,String reason){yes(!x,reason);}
 static boolean allowed(long actual,String status,String type,String drawer,
                        boolean member,boolean word){
   return KingLiveDrawRound9967.allowed(true,301,actual,status,type,
                drawer,"host",member,word);
 }
 public static void main(String[]args){
   yes(KingLiveDrawRound9967.validSketch(""),"Clearing own sketch permitted");
   yes(KingLiveDrawRound9967.validSketch("|100,200;111,205;|5,7;"),
       "Real DrawPad points accepted");
   no(KingLiveDrawRound9967.validSketch(null),"Null forbidden");
   no(KingLiveDrawRound9967.validSketch("300,400;bad"),"Untrusted stroke chars rejected");
   no(KingLiveDrawRound9967.validSketch(new String(new char[12001]).replace('\0','1')),
       "Unbounded drawing rejected");
   yes(allowed(301,"active","draw","host",true,true),
       "Real drawer can publish to live Party game");
   no(allowed(300,"active","draw","host",true,true),
       "Delayed stroke from previous round never touches next game");
   no(allowed(302,"active","draw","host",true,true),
       "Stale stroke cannot overwrite later real round");
   no(allowed(301,"closed","draw","host",true,true),
       "Finish round locks sketch");
   no(allowed(301,"active","spy","host",true,true),
       "Old Draw action cannot overwrite Spy game");
   no(allowed(301,"active","draw","cohost",true,true),
       "Co-host may not alter elected Drawer canvas");
   no(allowed(301,"active","draw","host",false,true),
       "Former member cannot draw");
   no(allowed(301,"active","draw","host",true,false),
       "No canvas before private drawing word loaded");
   no(KingLiveDrawRound9967.allowed(false,301,301,"active","draw","host","host",true,true),
       "No game document means no update");
   System.out.println("PASS real two-player Party drawing guards: current round, current drawer, active member, private word and stroke bounds");
 }
}