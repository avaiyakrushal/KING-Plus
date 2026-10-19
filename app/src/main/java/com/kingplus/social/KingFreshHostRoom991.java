package com.kingplus.social;

/** Safe fast-path solely for a host whose NEW room create was acknowledged by Firestore. */
public final class KingFreshHostRoom991 {
    private KingFreshHostRoom991(){}
    public static boolean canWriteWithoutPreRead(
            String newRoomId,String selectedRoomId,
            String ownerUid,String signedUid,boolean confirmedOwner){
        return confirmedOwner && newRoomId!=null && !newRoomId.isEmpty()
            && newRoomId.equals(selectedRoomId)
            && ownerUid!=null && !ownerUid.isEmpty()
            && ownerUid.equals(signedUid);
    }
}
