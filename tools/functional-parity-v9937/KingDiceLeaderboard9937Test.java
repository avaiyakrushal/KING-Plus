package com.kingplus.social;
public final class KingDiceLeaderboard9937Test {
 static void require(boolean x,String msg){if(!x)throw new AssertionError(msg);}
 public static void main(String[] args){
   require(KingDiceLeaderboard9937.parseRoll("🎲 1 / 6")==1,"min");
   require(KingDiceLeaderboard9937.parseRoll("🎲 6 / 6")==6,"max");
   require(KingDiceLeaderboard9937.parseRoll("🎲 7 / 6")==0,"invalid high");
   require(KingDiceLeaderboard9937.parseRoll("🎲 1 / 9")==0,"wrong dice");
   require(KingDiceLeaderboard9937.parseRoll("🎲 999 / 6")==0,"forged");
   require(KingDiceLeaderboard9937.parseRoll(null)==0,"null");
   KingDiceLeaderboard9937 board=new KingDiceLeaderboard9937();
   board.add("u1","A","🎲 4 / 6");
   board.add("u2","B","🎲 6 / 6");
   board.add("u1","A","🎲 5 / 6");
   board.add("u2","B","🎲 7 / 6");
   board.add("u1","A","not dice");
   board.add("","Ghost","🎲 6 / 6");
   String score=board.summary();
   require(score.contains("1. A • 9 points / 2 rolls"),"accumulated player A");
   require(score.contains("2. B • 6 points / 1 rolls"),"player B");
   require(!score.contains("Ghost"),"invalid uid ignored");
   System.out.println("PASS independent KING realtime Dice parse/score leaderboard");
 }
}
