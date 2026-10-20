package com.kingplus.social;
import java.util.Arrays;
/** Family roles: readable badges and strict client-side role boundaries. */
public final class KingMeFamilyRoles9986 {
 private KingMeFamilyRoles9986(){}
 public static boolean permitted(String role){
  return Arrays.asList("member","moderator","admin").contains(role);
 }
 public static boolean mayChange(String actorUid,String ownerUid,String targetUid,String nextRole){
  return actorUid!=null&&actorUid.equals(ownerUid)
    && targetUid!=null&&!targetUid.equals(ownerUid)&&permitted(nextRole);
 }
 public static String badge(String role) {
  if("owner".equals(role))return "👑 Owner";
  if("admin".equals(role))return "🛡 Admin";
  if("moderator".equals(role))return "⭐ Moderator";
  return "👤 Member";
 }
}
