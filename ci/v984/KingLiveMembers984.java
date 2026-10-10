package com.kingplus.social;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Privacy-safe, read-only Firestore member diagnostics for two-phone Party tests. */
public final class KingLiveMembers984 {
    private KingLiveMembers984() {}

    /** A delayed callback from a switched room, session or account must not
     * overwrite the current test or show another Room's membership. */
    public static boolean active(int requestedGeneration,int currentGeneration,
            String expectedUid,String currentUid,String expectedRoom,String currentRoom,
            boolean activityAlive) {
        return activityAlive && requestedGeneration>0
            && requestedGeneration==currentGeneration
            && expectedUid!=null&&!expectedUid.isEmpty()
            && expectedUid.equals(currentUid)
            && expectedRoom!=null&&!expectedRoom.isEmpty()
            && expectedRoom.equals(currentRoom);
    }

    public static String state(int count,boolean thisUidFound,boolean fromCache) {
        if(count<0)return "Room member records unavailable";
        String base="Firestore member records: "+count
            +" • This Firebase account: "+(thisUidFound?"PRESENT":"NOT PRESENT");
        return fromCache
            ? base+" • CACHED only (waiting for Firebase server)"
            : base+" • LIVE server snapshot";
    }

    /** Only display a few *redacted* member identifiers in a shared report. */
    public static String samplePrefixes(List<String> uids,int maxItems) {
        if(uids==null||uids.isEmpty()||maxItems<=0)return "none";
        int cap=Math.min(maxItems,8);
        Set<String> visited=new HashSet<>();
        List<String> displayed=new ArrayList<>();
        for(String uid:uids) {
            if(uid==null||uid.isEmpty()||!visited.add(uid))continue;
            displayed.add(KingConnectionDiagnostics983.uidPrefix(uid));
            if(displayed.size()>=cap)break;
        }
        return displayed.isEmpty()?"none":String.join(", ",displayed);
    }
}
