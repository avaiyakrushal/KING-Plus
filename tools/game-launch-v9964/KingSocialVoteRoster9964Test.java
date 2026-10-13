package com.kingplus.social;
import java.util.*;
public final class KingSocialVoteRoster9964Test {
 static void yes(boolean v,String message){if(!v)throw new AssertionError(message);}
 static void no(boolean v,String message){yes(!v,message);}
 public static void main(String[]args){
  List<String> frozen=Arrays.asList("host","alice","bob","dora");
  yes(KingSocialVoteRoster9964.isCurrent(true,22L,22L,"active","werewolf","werewolf"),
      "voting only in same active game and round");
  no(KingSocialVoteRoster9964.isCurrent(true,21L,22L,"active","werewolf","werewolf"),
      "stale voting and finish rejected");
  no(KingSocialVoteRoster9964.isCurrent(true,22L,22L,"closed","werewolf","werewolf"),
      "finished round cannot accept a vote");
  no(KingSocialVoteRoster9964.isCurrent(true,22L,22L,"active","spy","werewolf"),
      "new game must not be closed by stale Werewolf callback");
  no(KingSocialVoteRoster9964.isCurrent(false,22L,22L,"active","werewolf","werewolf"),
      "missing round rejected");
  no(KingSocialVoteRoster9964.isCurrent(true,null,22L,"active","werewolf","werewolf"),
      "missing round ID rejected");
  yes(KingSocialVoteRoster9964.validBallot("alice","bob",frozen,true,true),
      "actual joined and locked players");
  no(KingSocialVoteRoster9964.validBallot("alice","eve",frozen,true,true),
      "stale ghost outside participant UIDs cannot be nominated");
  no(KingSocialVoteRoster9964.validBallot("eve","bob",frozen,true,true),
      "spectator voter cannot vote");
  no(KingSocialVoteRoster9964.validBallot("alice","bob",frozen,false,true),
      "departed voter denied");
  no(KingSocialVoteRoster9964.validBallot("alice","bob",frozen,true,false),
      "departed nominated player denied");
  yes(KingSocialVoteRoster9964.validVoteDocument("alice","alice","bob",frozen),
      "valid vote count");
  no(KingSocialVoteRoster9964.validVoteDocument("alice","eve","bob",frozen),
      "forged voter identity ignored");
  no(KingSocialVoteRoster9964.validVoteDocument("alice","alice","eve",frozen),
      "ghost target ignored");
  no(KingSocialVoteRoster9964.validVoteDocument("eve","eve","bob",frozen),
      "spectator existing vote ignored");
  List<String> dedup=KingSocialVoteRoster9964.roster(Arrays.asList("alice","alice","bob",null,""));
  yes(dedup.size()==2&&dedup.get(0).equals("alice")&&dedup.get(1).equals("bob"),
      "clean unique frozen roster");
  yes(KingSocialVoteRoster9964.roster("invalid").isEmpty(),
      "invalid source does not elect players");
  System.out.println("PASS actual Party Werewolf/Spy roles: frozen live vote roster, no ghosts, no stale finish");
 }
}