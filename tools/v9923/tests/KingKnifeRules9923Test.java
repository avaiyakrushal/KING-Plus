package com.kingplus.social;
import java.util.ArrayList;
/** Pure Java tests for real wheel/knife collision and seven-target win. */
public final class KingKnifeRules9923Test {
  static void check(boolean yes,String why){if(!yes)throw new AssertionError(why);}
  public static void main(String[] args){
    ArrayList<Float> knives=new ArrayList<>();
    knives.add(0.0f);
    check(KingKnifeRules9923.hitsBlade(knives,0.05f),"overlapping knife must fail");
    check(!KingKnifeRules9923.hitsBlade(knives,0.9f),"distant knife must land");
    knives.clear();knives.add(3.07f);
    check(KingKnifeRules9923.hitsBlade(knives,-3.10f),"across +/-pi edge detects collision");
    check(!KingKnifeRules9923.roundCleared(6),"six knives not enough");
    check(KingKnifeRules9923.roundCleared(7),"seven nonoverlapping knives win");
    float a=KingKnifeRules9923.localAngle(0);
    float b=KingKnifeRules9923.localAngle((float)Math.PI);
    check(Math.abs(KingKnifeRules9923.normalize(a-b))>3f,"rotating wheel changes throw angle");
    System.out.println("PASS real knife wheel collision, wraparound, rotation and win conditions");
  }
}