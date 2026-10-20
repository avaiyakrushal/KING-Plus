package com.kingplus.social;
/** Protect authenticated multiplayer private-role fetch from Firestore/render
 * feedback loops. One request per actual round until explicit Retry.
 */
public final class KingPartyPrivateRoleLoad9966 {
 private KingPartyPrivateRoleLoad9966(){}
 public static boolean needsRequest(long current,long lastAttempt,
    boolean pending,String knownRole,String knownSecret,String type){
   if(current<=0||current==lastAttempt||pending)return false;
   if("draw".equals(type))
     return knownSecret==null||knownSecret.isEmpty();
   if("werewolf".equals(type)||"spy".equals(type))
     return knownRole==null||knownRole.isEmpty();
   return false;
 }
}
