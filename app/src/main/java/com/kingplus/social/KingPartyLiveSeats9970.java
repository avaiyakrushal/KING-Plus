package com.kingplus.social;
/** Real Party Stage supports 8, 10 or 12 seats. A seat taken on one
 * Android must be reconciled against every other live seat on SERVER.
 */
public final class KingPartyLiveSeats9970 {
 private KingPartyLiveSeats9970(){}
 public static boolean allowed(int maxSeats,int seatNumber){
   return (maxSeats==8||maxSeats==10||maxSeats==12)
      &&seatNumber>=1&&seatNumber<=maxSeats;
 }
 public static boolean owned(String account,String seatUid){
   return account!=null&&!account.isEmpty()&&account.equals(seatUid);
 }
}
