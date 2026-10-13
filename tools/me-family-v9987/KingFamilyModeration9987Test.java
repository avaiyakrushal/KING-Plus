package com.kingplus.social;
public final class KingFamilyModeration9987Test {
  private static void ok(boolean value,String name){if(!value)throw new AssertionError(name);}
  public static void main(String[] args){
    ok(KingFamilyModeration9987.canRemoveMember("owner123","owner123","owner","bob123","admin"),"owner can kick admin");
    ok(KingFamilyModeration9987.canRemoveMember("admin123","owner123","admin","bob123","member"),"admin can remove normal member");
    ok(!KingFamilyModeration9987.canRemoveMember("admin123","owner123","admin","bob123","moderator"),"admin cannot kick moderator");
    ok(!KingFamilyModeration9987.canRemoveMember("mod123","owner123","moderator","bob123","member"),"moderator cannot kick");
    ok(!KingFamilyModeration9987.canRemoveMember("admin123","owner123","admin","owner123","owner"),"never kick owner");
    ok(!KingFamilyModeration9987.canRemoveMember("admin123","owner123","admin","admin123","admin"),"cannot self-kick through admin UI");
    ok(KingFamilyModeration9987.canModerateMessage("mod123","owner123","moderator","bob123"),"moderator can moderate chat");
    ok(KingFamilyModeration9987.canModerateMessage("admin123","owner123","admin","bob123"),"admin can moderate chat");
    ok(KingFamilyModeration9987.canModerateMessage("bob123","owner123","member","bob123"),"sender can delete own chat");
    ok(!KingFamilyModeration9987.canModerateMessage("bob123","owner123","member","alice123"),"member cannot delete others");
    ok("⭐".equals(KingFamilyModeration9987.roleBadge("moderator")),"role badge");
    System.out.println("PASS 11 Family Admin/Moderator real permissions and owner protection cases");
  }
}
