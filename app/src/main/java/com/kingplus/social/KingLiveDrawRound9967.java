package com.kingplus.social;
/** Pure guards for actual signed-in Party Draw actions and serial strokes. */
public final class KingLiveDrawRound9967 {
 private KingLiveDrawRound9967(){}
 public static boolean validSketch(String sketch){
   if(sketch==null||sketch.length()>12000)return false;
   for(int i=0;i<sketch.length();i++){
     char ch=sketch.charAt(i);
     if(!((ch>='0'&&ch<='9')||ch==','||ch==';'||ch=='|'))return false;
   }
   return true;
 }
 public static boolean allowed(boolean stateExists,long expected,long actual,
         String status,String type,String drawerUid,String actorUid,
         boolean memberPresent,boolean confirmedPrivateWord){
   return stateExists&&expected>0&&expected==actual&&memberPresent
     &&confirmedPrivateWord&&"active".equals(status)&&"draw".equals(type)
     &&drawerUid!=null&&!drawerUid.isEmpty()&&drawerUid.equals(actorUid);
 }
}
