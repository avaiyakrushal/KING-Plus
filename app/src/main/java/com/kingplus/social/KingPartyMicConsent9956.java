package com.kingplus.social;
public final class KingPartyMicConsent9956 {
 private KingPartyMicConsent9956(){}
 public static boolean canTransmit(boolean approved,boolean cloud,
      String room,String approvalRoom,int seat,int approvalSeat,
      String user,String seatOwner,boolean backendMic,boolean muteAll,boolean moderator){
    return approved&&cloud&&room!=null&&!room.isEmpty()
       &&room.equals(approvalRoom)&&seat>0&&seat==approvalSeat
       &&user!=null&&!user.isEmpty()&&user.equals(seatOwner)
       &&backendMic&&(!muteAll||moderator);
 }
}
