package com.kingplus.social;
import java.util.Map;
public final class KingLiveGuard10046Test {
  private static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);}
  public static void main(String[] args){
    for(int level:new int[]{-1,0,1,9,100}){
      for(long xp:new long[]{-100L,0L,100L,123456789L}){
        Map<String,Object> p=KingLiveGuard10046.progressPatch(level,xp,"BRONZE");
        check(p.size()==3,"Only progression keys allowed");
        for(String protectedKey:new String[]{"publicId","vipLevel","vipPoints","vipNumber","nameColor","displayName","searchName","photoUrl","uid"})
          check(!p.containsKey(protectedKey),"Protected field overwritten: "+protectedKey);
        check(((Integer)p.get("level"))>=1,"Level below minimum");
        check(((Long)p.get("xp"))>=0,"XP below zero");
      }
    }
    check("".equals(KingLiveGuard10046.progressPatch(1,0,null).get("levelTier")),"null tier unsafe");
    System.out.println("KING v10.0.46 profile/VIP immutable-field tests passed");
  }
}
