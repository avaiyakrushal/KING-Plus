package com.kingplus.social;
import java.util.*;
/** Pure logic for validated, frozen, live Party game vote participants. */
public final class KingSocialVoteRoster9964 {
 private KingSocialVoteRoster9964(){}
 public static boolean isCurrent(boolean exists,Long stored,long expected,
                                 String status,String type,String required){
   return exists&&stored!=null&&expected>0&&stored.longValue()==expected
      &&"active".equals(status)&&required!=null&&required.equals(type);
 }
 public static List<String> roster(Object raw){
   if(!(raw instanceof List))return Collections.emptyList();
   List<String> out=new ArrayList<>();
   for(Object v:(List<?>)raw){
     if(v instanceof String&&!((String)v).isEmpty()&&!out.contains(v))
       out.add((String)v);
   }
   return out;
 }
 public static boolean validBallot(String voter,String target,List<String> locked,
                                   boolean voterPresent,boolean targetPresent){
   return voter!=null&&target!=null&&locked!=null&&voterPresent&&targetPresent
       &&locked.contains(voter)&&locked.contains(target);
 }
 public static boolean validVoteDocument(String documentUid,String claimedUid,
                                         String choice,List<String> locked){
   return documentUid!=null&&!documentUid.isEmpty()
      &&documentUid.equals(claimedUid)&&choice!=null
      &&locked!=null&&locked.contains(documentUid)&&locked.contains(choice);
 }
}
