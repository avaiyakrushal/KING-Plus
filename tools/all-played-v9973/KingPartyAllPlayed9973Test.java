package com.kingplus.social;
import java.util.*;
public final class KingPartyAllPlayed9973Test {
 static void yes(boolean v,String m){if(!v)throw new AssertionError(m);}
 static void no(boolean v,String m){yes(!v,m);}
 static boolean finish(String game,List<String> roster,List<String> moves){
   return KingPartyAllPlayed9973.shouldFinish(game,"active",true,true,true,313,roster,moves);
 }
 public static void main(String[] args){
   String[] games={"rps","dice","number","coin","wheel","bingo","slot","reaction","highlow","sheep","zoo","memory","domino"};
   List<String> four=Arrays.asList("alice","bob","cara","dan");
   for(String game:games){
      no(finish(game,four,Arrays.asList("alice","bob")),game+" must not settle 2 of 4 Ready");
      no(finish(game,four,Arrays.asList("alice","bob","cara")),game+" must wait for last player");
      yes(finish(game,four,Arrays.asList("alice","bob","cara","dan")),game+" settles only 4 of 4");
      yes(finish(game,four,Arrays.asList("dan","cara","bob","alice")),game+" order independent");
      no(finish(game,four,Arrays.asList("alice","bob","cara","eve")),game+" spectator may not replace missing player");
      no(finish(game,four,Arrays.asList("alice","bob","cara","cara")),game+" duplicate may not fake last player");
      no(finish(game,four,Arrays.asList("alice","bob","cara","dan","eve")),game+" ghost extras must not decide outcome");
   }
   no(finish("draw",four,four),"Draw private-role settlement stays separate");
   no(finish("spy",four,four),"Spy vote settlement stays separate");
   no(finish("ttt",four,four),"TTT turn engine stays separate");
   no(KingPartyAllPlayed9973.shouldFinish("dice","closed",true,true,true,313,four,four),"closed cannot settle twice");
   no(KingPartyAllPlayed9973.shouldFinish("dice","active",false,true,true,313,four,four),"nonhost cannot settle");
   no(KingPartyAllPlayed9973.shouldFinish("dice","active",true,true,false,313,four,four),"offline cache not trusted");
   no(KingPartyAllPlayed9973.shouldFinish("dice","active",true,false,true,313,four,four),"kicked moderator cannot settle");
   no(KingPartyAllPlayed9973.shouldFinish("dice","active",true,true,true,0,four,four),"missing round cannot settle");
   System.out.println("PASS all 13 Party Games require ALL 4 of 4 Ready moves; no premature winner or ghost settlement");
 }
}