package com.kingplus.social;
/** Tiny auditable helper used on fresh SERVER transaction documents. */
public final class KingTttLiveRoster9951 {
  private KingTttLiveRoster9951(){}
  /** Explicit Ready flag must still be true in the transaction's SERVER read. */
  public static boolean valid(String expectedUid,
      boolean readyExists,String readyUid,Boolean readyFlag,
      boolean memberExists,String memberUid){
    return Boolean.TRUE.equals(readyFlag)
       && valid(expectedUid,readyExists,readyUid,memberExists,memberUid);
  }
  public static boolean valid(String expectedUid,
      boolean readyExists,String readyUid,boolean memberExists,String memberUid){
    return expectedUid!=null&&!expectedUid.isEmpty()
       &&readyExists&&memberExists
       &&expectedUid.equals(readyUid)&&expectedUid.equals(memberUid);
  }
}
