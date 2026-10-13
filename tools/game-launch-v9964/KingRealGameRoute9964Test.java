package com.kingplus.social;
public final class KingRealGameRoute9964Test {
 static void yes(boolean x,String m){if(!x)throw new AssertionError(m);}
 public static void main(String[]args){
  for(String game:new String[]{"Ludo Master","Ludo Voice","Sheep Fight","Werewolf","Werewolf Team","Spy Game","Draw & Guess"}){
   yes(KingRealGameRoute9964.route(game)==KingRealGameRoute9964.PARTY_GAME,
        "real multiplayer entry required for "+game);
  }
  yes(KingRealGameRoute9964.route("Free Fire MAX")==KingRealGameRoute9964.VOICE_ONLY,
      "Do not falsely embed another company's game");
  for(String game:new String[]{"Bingo","Crazy Zoo","Chess","Fishing","Bubble Shooter","",null}){
   yes(KingRealGameRoute9964.route(game)==KingRealGameRoute9964.NOT_READY,
       "No manufactured winnings for game "+game);
  }
  yes(KingRealGameRoute9964.route("  LUDO Master ")==KingRealGameRoute9964.PARTY_GAME,
      "trimmed route");
  System.out.println("PASS real Party game entry, unsupported modes disclose unavailability, no fake GP");
 }
}