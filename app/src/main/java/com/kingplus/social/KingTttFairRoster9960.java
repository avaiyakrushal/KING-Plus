package com.kingplus.social;
import java.security.SecureRandom;
/** Uniformly select two DIFFERENT current Party Ready members for live X/O. */
public final class KingTttFairRoster9960 {
  private KingTttFairRoster9960(){}
  public static int[] pickPair(int eligibleCount,SecureRandom random){
    if(eligibleCount<2||eligibleCount>50||random==null)
      throw new IllegalArgumentException("Need 2-50 live Ready Party players");
    final int x=random.nextInt(eligibleCount);
    final int other=random.nextInt(eligibleCount-1);
    // Exclude X, keep O uniform across every remaining player.
    final int o=other>=x?other+1:other;
    return new int[]{x,o};
  }
}
