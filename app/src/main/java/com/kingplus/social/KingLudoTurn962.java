package com.kingplus.social;

import java.util.List;

/** Pure, deterministic guards for KING Plus multiplayer Ludo UI actions.
 * Firestore rules mirror critical permissions; never treat UI guards as authorization.
 */
public final class KingLudoTurn962 {
    public static final long INACTIVE_TURN_MS = 60_000L;
    private KingLudoTurn962() {}

    public static boolean canStart(String actorUid, String ownerUid, String phase,
                                   List<String> players, List<Boolean> ready, int capacity) {
        if (actorUid == null || actorUid.isEmpty() || !actorUid.equals(ownerUid)
                || !"lobby".equals(phase) || players == null || ready == null
                || players.size() < 4 || ready.size() < 4 || (capacity != 2 && capacity != 4)) {
            return false;
        }
        if (!occupied(players, 0) || !occupied(players, 2)
                || !Boolean.TRUE.equals(ready.get(0))
                || !Boolean.TRUE.equals(ready.get(2))) return false;
        if (capacity == 2) return true;
        return occupied(players, 1) && occupied(players, 3)
                && Boolean.TRUE.equals(ready.get(1))
                && Boolean.TRUE.equals(ready.get(3));
    }

    public static boolean canSkip(String actorUid, String phase, int turn,
                                  List<String> players, long lastActionTimeMs, long nowMs) {
        if (actorUid == null || actorUid.isEmpty() || players == null
                || players.size() < 4 || turn < 0 || turn >= 4
                || (!"roll".equals(phase) && !"move".equals(phase)
                    && !"resolve".equals(phase)) || lastActionTimeMs <= 0
                || nowMs < lastActionTimeMs
                || nowMs - lastActionTimeMs < INACTIVE_TURN_MS) return false;
        if (actorUid.equals(players.get(turn))) return false; // Current player cannot forfeit others.
        for (String uid : players) {
            if (actorUid.equals(uid)) return true;
        }
        return false;
    }

    private static boolean occupied(List<String> players, int index) {
        String uid = players.get(index);
        return uid != null && !uid.trim().isEmpty();
    }
}
