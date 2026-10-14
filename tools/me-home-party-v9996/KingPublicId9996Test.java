package com.kingplus.social;
public final class KingPublicId9996Test {
 public static void main(String[] args) {
  for(String invalid: new String[]{null,"","123","abcdef","1234567","123 456","012345x"})
   if(KingPublicId9996.verified(invalid)!=null)
    throw new AssertionError("Bad KING ID accepted: "+invalid);
  if(!"001234".equals(KingPublicId9996.verified(" 001234 ")))
   throw new AssertionError("Issued KING ID changed");
  System.out.println("PASS only actual six-digit KING IDs can be displayed/shared");
 }
}
