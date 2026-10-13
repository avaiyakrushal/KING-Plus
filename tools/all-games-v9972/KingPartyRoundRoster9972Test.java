package com.kingplus.social;
import java.util.*;
public final class KingPartyRoundRoster9972Test {
 static void check(boolean value,String msg){if(!value)throw new AssertionError(msg);}
 public static void main(String[]args){
  String[] games={"rps","dice","number","coin","wheel","bingo","slot","reaction","highlow","sheep","zoo","memory","domino"};
  for(String g:games)check(KingPartyRoundRoster9972.isOrdinaryGame(g),"Actual Party game missing:"+g);
  for(String g:new String[]{"draw","spy","werewolf","ttt","ludo","unknown"})
   check(!KingPartyRoundRoster9972.isOrdinaryGame(g),"Do not bypass specialized multiplayer:"+g);
  List<String> live=Arrays.asList("alice","bob");
  check(KingPartyRoundRoster9972.valid(live),"Two authenticated players can begin round");
  check(KingPartyRoundRoster9972.realReady("alice","alice",true,true),"Real ready");
  check(!KingPartyRoundRoster9972.realReady("alice","alice",false,true),"False Ready ignored");
  check(!KingPartyRoundRoster9972.realReady("ghost","ghost",true,false),"Ghost ready ignored");
  check(!KingPartyRoundRoster9972.realReady("alice","eve",true,true),"Forged Ready UID ignored");
  check(KingPartyRoundRoster9972.canPlay(live,"bob"),"Selected Bob can make a move");
  check(!KingPartyRoundRoster9972.canPlay(live,"eve"),"Spectator Eve cannot move");
  check(KingPartyRoundRoster9972.containsOnly(live,Arrays.asList("bob","alice")),"Two genuine submitted moves");
  check(!KingPartyRoundRoster9972.containsOnly(live,Arrays.asList("bob","eve")),"Spectator cannot alter winner");
  check(!KingPartyRoundRoster9972.containsOnly(live,Arrays.asList("alice","alice")),"Duplicate score cannot forge winner");
  check(!KingPartyRoundRoster9972.containsOnly(live,Arrays.asList("alice")),"Single player cannot finish");
  List<String> tooMany=new ArrayList<>();for(int i=0;i<51;i++)tooMany.add("player"+i);
  check(!KingPartyRoundRoster9972.valid(tooMany),"Max 50 actual ready players");
  check(KingPartyRoundRoster9972.asRoster(Arrays.asList("alice","bob")).size()==2,"Read live frozen roster");
  check(KingPartyRoundRoster9972.asRoster(Arrays.asList("alice",13)).isEmpty(),"Invalid Firestore roster denied");
  check(KingPartyRoundRoster9972.asRoster(null).isEmpty(),"Missing Firestore roster denied");
  System.out.println("PASS 13 real Party games use frozen active-ready player list; ghost, duplicate and spectator results rejected");
 }
}