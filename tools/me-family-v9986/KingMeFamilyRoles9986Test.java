package com.kingplus.social;
public final class KingMeFamilyRoles9986Test {
 static void check(boolean v,String message){if(!v)throw new AssertionError(message);}
 public static void main(String[] args){
  check(KingMeFamilyRoles9986.mayChange("owner123","owner123","agent123","admin"),"owner grants admin");
  check(KingMeFamilyRoles9986.mayChange("owner123","owner123","agent123","moderator"),"owner grants moderator");
  check(KingMeFamilyRoles9986.mayChange("owner123","owner123","agent123","member"),"owner demotes");
  check(!KingMeFamilyRoles9986.mayChange("agent123","owner123","agent123","admin"),"member no self promote");
  check(!KingMeFamilyRoles9986.mayChange("owner123","owner123","owner123","member"),"no owner demote");
  check(!KingMeFamilyRoles9986.mayChange("owner123","owner123","agent123","owner"),"no second owner");
  check("👑 Owner".equals(KingMeFamilyRoles9986.badge("owner")),"readable owner");
  System.out.println("PASS 7 Family role-management and original Owner protections");
 }
}
