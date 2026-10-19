package com.kingplus.social;
import java.util.Objects;
/** Replace room-game Firestore move listener only for actual match identity changes. */
public final class KingPartyMoveSubscription9979 {
  private KingPartyMoveSubscription9979() {}
  public static boolean needsNewListener(boolean attached,long observedRound,
                                            String observedType,long currentRound,String currentType){
    return !attached || observedRound!=currentRound ||
      !Objects.equals(observedType,currentType);
  }
}
