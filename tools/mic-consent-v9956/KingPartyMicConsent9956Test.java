package com.kingplus.social;
public final class KingPartyMicConsent9956Test {
 static void ok(boolean c,String m){if(!c)throw new AssertionError(m);}
 static void no(boolean c,String m){ok(!c,m);}
 static boolean safe(boolean approved,boolean cloud,String room,String original,
    int seat,int oldSeat,String uid,String seatUid,boolean backend,boolean muteAll,boolean mod){
   return KingPartyMicConsent9956.canTransmit(approved,cloud,room,original,seat,oldSeat,
         uid,seatUid,backend,muteAll,mod);
 }
 public static void main(String[]args){
   ok(safe(true,true,"a","a",2,2,"alice","alice",true,false,false),"only Alice enables own Mic");
   no(safe(false,true,"a","a",2,2,"alice","alice",true,false,false),"remote unmute not permission");
   no(safe(true,true,"b","a",2,2,"alice","alice",true,false,false),"old room");
   no(safe(true,true,"a","a",3,2,"alice","alice",true,false,false),"old seat");
   no(safe(true,true,"a","a",2,2,"alice","bob",true,false,false),"stolen seat");
   no(safe(true,true,"a","a",2,2,"alice","alice",false,false,false),"host mute");
   no(safe(true,true,"a","a",2,2,"alice","alice",true,true,false),"mute all");
   ok(safe(true,true,"a","a",2,2,"alice","alice",true,true,true),"moderator explicit own tap");
   no(safe(true,false,"a","a",2,2,"alice","alice",true,false,false),"local fake room");
   no(safe(true,true,"a","a",-1,2,"alice","alice",true,false,false),"no seat");
   System.out.println("PASS actual Party mic: local owner consent required on real room+seat");
 }
}