package com.kingplus.social;

import java.util.*;
/** Real Me Profile gift history: never invent receipt rows or aggregate other users' gifts. */
public final class KingMeGiftSummary9983 {
 private KingMeGiftSummary9983(){}
 public static final class Gift {
  public final String id, senderUid, targetUid, name;
  public final long amount, createdMs;
  public Gift(String id,String senderUid,String targetUid,String name,long amount,long createdMs){
   this.id=id;this.senderUid=senderUid;this.targetUid=targetUid;this.name=name;
   this.amount=amount;this.createdMs=createdMs;
  }
 }
 public static List<Gift> forAccount(List<Gift> records,String owner,boolean receivedOnly) {
  if(owner==null||owner.isEmpty()||records==null)return Collections.emptyList();
  Map<String,Gift> byId=new LinkedHashMap<>();
  for(Gift g:records){
   if(g==null||g.id==null||g.id.trim().isEmpty()||g.name==null||g.name.trim().isEmpty())continue;
   if(!owner.equals(g.targetUid) && (receivedOnly||!owner.equals(g.senderUid)))continue;
   byId.putIfAbsent(g.id,g);
  }
  List<Gift> result=new ArrayList<>(byId.values());
  result.sort((a,b)->Long.compare(b.createdMs,a.createdMs));
  return result;
 }
 public static Map<String,Integer> receivedWall(List<Gift> records,String owner){
  Map<String,Integer> count=new LinkedHashMap<>();
  for(Gift g:forAccount(records,owner,true))count.put(g.name,Math.min(999999,
     count.getOrDefault(g.name,0)+1));
  return count;
 }
 public static boolean validWish(String value){
  return value!=null&&Arrays.asList("Royal Castle","Galaxy Ship","King Dragon",
    "VIP Crown","Diamond Rain").contains(value);
 }
}
