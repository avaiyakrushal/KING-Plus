package com.kingplus.social;
import java.util.*;
/** Owner-scoped Me preferences: toggles are never treated as diamond balances. */
public final class KingMePrivacyPolicy9984 {
 private KingMePrivacyPolicy9984(){}
 public static final String[] PRIVACY={
  "findMe","showOnlineStatus","allowRoomInvites","allowPrivateMessages","showEntranceEffects"
 };
 public static final String[] NOTIFY={
  "notifyMessages","notifySocial","notifyRoomInvites","notifyGifts","notifyGames"
 };
 public static final String[] PRIVACY_LABELS={
  "Allow people to find me","Show online status","Allow Party Room invitations",
  "Allow private messages","Show entrance effects"
 };
 public static final String[] NOTIFY_LABELS={
  "Messages","Followers and friends","Room invitations","Gifts and VIP","Game invitations"
 };
 public static boolean permitted(String key){
  for(String x:PRIVACY)if(x.equals(key))return true;
  for(String x:NOTIFY)if(x.equals(key))return true;
  return false;
 }
 public static boolean validUid(String uid){
  if(uid==null||uid.length()<4||uid.length()>128)return false;
  for(int i=0;i<uid.length();i++)if(uid.charAt(i)=='/'||uid.charAt(i)<33)return false;
  return !uid.trim().isEmpty();
 }
 public static Map<String,Object> defaults(String uid){
  Map<String,Object> result=new HashMap<>();
  result.put("uid",uid);
  for(String x:PRIVACY)result.put(x,true);
  for(String x:NOTIFY)result.put(x,true);
  return result;
 }
}
