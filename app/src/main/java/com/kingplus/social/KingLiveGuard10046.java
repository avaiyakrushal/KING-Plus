package com.kingplus.social;

import java.util.HashMap;
import java.util.Map;

/** Client-side invariant: gameplay XP cannot issue or erase paid identity. */
public final class KingLiveGuard10046 {
    private KingLiveGuard10046() {}
    public static Map<String,Object> progressPatch(int level,long xp,String tier){
        Map<String,Object> patch=new HashMap<>();
        patch.put("level",Math.max(1,level));
        patch.put("xp",Math.max(0L,xp));
        patch.put("levelTier",tier==null?"":tier);
        return patch;
    }
}
