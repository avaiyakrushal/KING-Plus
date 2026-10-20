package com.kingplus.social;

/** Family role moderation with no artificial monetary entitlements. */
public final class KingFamilyModeration9987 {
  private KingFamilyModeration9987(){}
  public static boolean canRemoveMember(String actingUid,String ownerUid,String actorRole,
      String targetUid,String targetRole) {
    if(actingUid==null||targetUid==null||actingUid.equals(targetUid)||
       targetUid.equals(ownerUid))return false;
    if(actingUid.equals(ownerUid))return true;
    // Family administrators can remove normal members but cannot displace
    // other admins or moderators; moderator may only moderate Family chat.
    return "admin".equals(actorRole) && "member".equals(targetRole);
  }
  public static boolean canModerateMessage(String actingUid,String ownerUid,
      String actorRole,String senderUid) {
    if(actingUid==null)return false;
    return actingUid.equals(ownerUid)||actingUid.equals(senderUid)
      ||"admin".equals(actorRole)||"moderator".equals(actorRole);
  }
  public static String roleBadge(String role) {
    if("owner".equals(role))return "👑";
    if("admin".equals(role))return "🛡";
    if("moderator".equals(role))return "⭐";
    return "👤";
  }
}
