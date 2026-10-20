package com.kingplus.social;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Validates Firebase-authenticated realtime social callbacks and follower sets. */
public final class KingSocialRealtime970 {
    private KingSocialRealtime970(){}

    public static boolean activeAccount(String expectedUid,String currentUid,boolean visible){
        return visible && expectedUid!=null && !expectedUid.isEmpty()
            && expectedUid.equals(currentUid);
    }
    public static boolean isCurrentView(String requested,String selected,
                                        int requestGeneration,int activeGeneration,
                                        boolean visible){
        return visible && requested!=null && requested.equals(selected)
            && requestGeneration==activeGeneration && requestGeneration>0;
    }
    public static boolean sameQuery(String requested,String current){
        return requested!=null && current!=null && requested.trim().equals(current.trim());
    }
    public static Set<String> friends(Collection<String> following,Collection<String> followers){
        HashSet<String> result=new HashSet<>();
        if(following==null||followers==null)return result;
        for(String uid:following)if(uid!=null&&!uid.isEmpty()&&followers.contains(uid))
            result.add(uid);
        return result;
    }
    public static boolean showUid(String uid,String ownUid){
        return uid!=null&&!uid.isEmpty()&&!uid.equals(ownUid);
    }
}
