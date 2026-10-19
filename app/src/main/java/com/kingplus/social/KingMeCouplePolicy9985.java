package com.kingplus.social;
import java.util.*;
/** Identity and consent rules for real KING Plus Relationship/CP. */
public final class KingMeCouplePolicy9985 {
 private KingMeCouplePolicy9985(){}
 public static boolean validUid(String u){
  if(u==null||u.length()<4||u.length()>128)return false;
  for(int i=0;i<u.length();i++){char c=u.charAt(i);if(c=='/'||c<33)return false;}
  return true;
 }
 public static String pairId(String a,String b){
  if(!validUid(a)||!validUid(b)||a.equals(b))throw new IllegalArgumentException("two different valid UIDs");
  return a.compareTo(b)<0?a+"__"+b:b+"__"+a;
 }
 public static boolean canAccept(String requester,String recipient,String actor,String status){
  return validUid(requester)&&validUid(recipient)&&!requester.equals(recipient)
   &&recipient.equals(actor)&&"pending".equals(status);
 }
 public static int countAccepted(String me,List<String[]> accepted){
  if(!validUid(me)||accepted==null)return 0;
  Set<String> peerUids=new HashSet<>();
  for(String[] item:accepted){
   if(item==null||item.length<3||!"accepted".equals(item[2]))continue;
   if(me.equals(item[0])&&validUid(item[1]))peerUids.add(item[1]);
   if(me.equals(item[1])&&validUid(item[0]))peerUids.add(item[0]);
  }
  return peerUids.size();
 }
}
