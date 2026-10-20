package com.kingplus.social;
import java.security.SecureRandom;
/** Independent original KING local game helpers.
 * RNG strengthens honest client rolls; server-proven fairness requires a
 * deployed trusted backend and is NOT implied by this class.
 */
public final class KingPartyGameFairPlay9949 {
 private KingPartyGameFairPlay9949(){}
 private static final SecureRandom RNG=new SecureRandom();
 public static int dice(){return 1+RNG.nextInt(6);}
 public static int wheel(){return 1+RNG.nextInt(100);}
 public static int slotDigit(){return RNG.nextInt(10);}
 public static boolean currentMove(long activeRound,String game,long moveRound,
                                   String moveGame,String docUid,String claimedUid){
   return activeRound>0&&activeRound==moveRound
      &&game!=null&&!game.isEmpty()&&game.equals(moveGame)
      &&docUid!=null&&!docUid.isEmpty()&&docUid.equals(claimedUid);
 }
 public static String visibleMove(String game,String status,boolean serverVerified,String actualMove){
   if("treasure".equals(game)&&(!serverVerified||!"closed".equals(status)))return "choice locked ✓";
   if("rps".equals(game)){
      if(!serverVerified||!"closed".equals(status))return "move locked ✓";
      if("Rock".equals(actualMove))return "✊ Rock";
      if("Paper".equals(actualMove))return "✋ Paper";
      if("Scissors".equals(actualMove))return "✌ Scissors";
      return "Invalid RPS move";
   }
   return actualMove==null?"":actualMove;
 }
}
