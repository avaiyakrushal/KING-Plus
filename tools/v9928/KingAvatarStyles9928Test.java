package com.kingplus.social;
public final class KingAvatarStyles9928Test {
  private static void check(boolean ok,String reason){if(!ok)throw new AssertionError(reason);}
  public static void main(String[] args){
    check(KingAvatarStyles9928.COUNT==6,"six genuine styles");
    check(KingAvatarStyles9928.normalize(-1)==0,"negative style safely normalized");
    check(KingAvatarStyles9928.normalize(7)==0,"unknown style safely normalized");
    check(KingAvatarStyles9928.defaultFor("alice")==KingAvatarStyles9928.defaultFor("alice"),"stable identity default");
    float[] first=KingAvatarStyles9928.color(2);
    first[0]=0f;
    check(KingAvatarStyles9928.color(2)[0]>0f,"presets are not shared mutable colors");
    for(int i=0;i<6;i++)check(!KingAvatarStyles9928.name(i).isEmpty(),"preset has name");
    System.out.println("PASS six original avatar outfits, stable fallback style and no shared mutable color");
  }
}