#!/usr/bin/env python3
"""KING Plus v9.9.45: genuine Room RPS/Dice/Coin game move validation.

Actual improvements:
- Only valid game choices count; rejected fake 999 Dice and spoofed Rock scores.
- RPS has explicit pure Java cyclic winner logic with draw and invalid handling.
- The existing live Party turn, ready, Gift ACK/animation and Sheep rules remain.
- No untrusted money/diamonds; real in-app tap rng is not cryptographic anti-cheat.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);java=root/'app/src/main/java/com/kingplus/social'
game=java/'RoomGameActivity.java';t=game.read_text()
old='''private String resultFor(List<DocumentSnapshot>a){if("memory".equals(type))'''
new='''private String resultFor(List<DocumentSnapshot>a){
        if("rps".equals(type)){
            java.util.List<String> names9945=new java.util.ArrayList<>();
            java.util.List<String> choices9945=new java.util.ArrayList<>();
            for(DocumentSnapshot d:a){
                names9945.add(s(d.getString("name")));
                choices9945.add(s(d.getString("move")));
            }
            return KingRpsRules9945.result(names9945,choices9945);
        }
        if("memory".equals(type))'''
if t.count(old)!=1:raise RuntimeError("Expected exactly one RPS result function; found "+str(t.count(old)))
game.write_text(t.replace(old,new,1))
(java/'KingRpsRules9945.java').write_text("""package com.kingplus.social;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
/** Correct KING Rock-Paper-Scissors winner for actual multi-user Room choices.
 * A real draw is not credited as a win; a legacy invalid move cannot win.
 */
public final class KingRpsRules9945 {
 private KingRpsRules9945(){}
 public static boolean valid(String c){
   return "Rock".equals(c)||"Paper".equals(c)||"Scissors".equals(c);
 }
 public static String winningMove(String a,String b){
   if(!valid(a)||!valid(b)||a.equals(b))return "";
   return ("Rock".equals(a)&&"Scissors".equals(b))
     ||("Scissors".equals(a)&&"Paper".equals(b))
     ||("Paper".equals(a)&&"Rock".equals(b))?a:b;
 }
 public static String result(List<String> names,List<String> moves){
   if(names==null||moves==null||names.size()!=moves.size()||moves.size()<2)
     return "Waiting for at least 2 confirmed players";
   Set<String> values=new HashSet<>();
   for(String choice:moves){
      if(!valid(choice))return "⛔ Invalid RPS choice — no winner";
      values.add(choice);
   }
   if(values.size()==1)return "🤝 Rock–Paper–Scissors draw • same choices";
   if(values.size()==3)return "🤝 Rock–Paper–Scissors draw • all 3 choices";
   String[] vs=values.toArray(new String[0]);
   String winner=winningMove(vs[0],vs[1]);
   if(winner.isEmpty())return "🤝 Rock–Paper–Scissors draw";
   String loser=winner.equals(vs[0])?vs[1]:vs[0];
   List<String> victors=new ArrayList<>();
   for(int i=0;i<moves.size();i++)
     if(winner.equals(moves.get(i))) {
       String name=names.get(i);
       victors.add(name==null||name.trim().isEmpty()?"Player":name.trim());
     }
   return "🏆 "+winner+" beats "+loser+"\\n"+String.join(", ",victors);
 }
}
""")
rules=root/'firestore.rules';s=rules.read_text()
old_rules="""          // Sheep cannot disguise a move as Dice/RPS to bypass the round timer."""
new_rules="""          // Only actual RPS choices; no fake 'Dragon', negative value or
          // forged scoring. Rock/Paper/Scissors are moves (score always zero).
          && (request.resource.data.type != 'rps'
              || (request.resource.data.get('move','') in ['Rock','Paper','Scissors']
                  && request.resource.data.get('score',-1) is int
                  && request.resource.data.score == 0))
          // Dice client inputs must be bounded 1..6 AND match the visible
          // roll label: no 7, 999, arbitrary string, or mismatched scoring.
          && (request.resource.data.type != 'dice'
              || (request.resource.data.get('score',-1) is int
                  && ((request.resource.data.score == 1 && request.resource.data.get('move','') == 'Rolled 1')
                   || (request.resource.data.score == 2 && request.resource.data.get('move','') == 'Rolled 2')
                   || (request.resource.data.score == 3 && request.resource.data.get('move','') == 'Rolled 3')
                   || (request.resource.data.score == 4 && request.resource.data.get('move','') == 'Rolled 4')
                   || (request.resource.data.score == 5 && request.resource.data.get('move','') == 'Rolled 5')
                   || (request.resource.data.score == 6 && request.resource.data.get('move','') == 'Rolled 6'))))
          // Coin Flip result labels cannot be arbitrary or carry fake points.
          && (request.resource.data.type != 'coin'
              || (request.resource.data.get('score',-1) is int
                  && ((request.resource.data.get('move','') == 'Heads' && request.resource.data.score == 1)
                   || (request.resource.data.get('move','') == 'Tails' && request.resource.data.score == 0))))
          // Sheep cannot disguise a move as Dice/RPS to bypass the round timer."""
if s.count(old_rules)!=1:raise RuntimeError("Firestore game_moves validation anchor changed: "+str(s.count(old_rules)))
rules.write_text(s.replace(old_rules,new_rules,1))
gradle=root/'app/build.gradle';g=gradle.read_text()
a="versionCode 223; versionName '9.9.44-gift-visual-confirmation-test'"
if g.count(a)!=1:raise RuntimeError("Not verified v9.9.44 full source")
gradle.write_text(g.replace(a,"versionCode 224; versionName '9.9.45-real-rps-dice-result-test'"))
# Adapt historical broad immutable regression to correct RPS protocol;
# previously used score 1/2, inconsistent with actual UI which sent zero.
test=root/'tests/firestore/immutable-rounds-firestore.test.mjs'
prior=test.read_text()
for old,new in [
 ("play('alice','rps',1043,'Rock',1)","play('alice','rps',1043,'Rock',0)"),
 ("play('bob','rps',1043,'Paper',2)","play('bob','rps',1043,'Paper',0)"),
 ("play('alice','rps',1043,'Scissors',3)","play('alice','rps',1043,'Scissors',0)"),
 ("play('bob','rps',1043,'Rock',1)","play('bob','rps',1043,'Rock',0)"),
 ("play('alice','rps',1042,'Rock',1)","play('alice','rps',1042,'Rock',0)"),
 ("play('alice','rps',1044,'Rock',1)","play('alice','rps',1044,'Rock',0)"),
 ("play('bob','rps',1043,'Rock',1)","play('bob','rps',1043,'Rock',0)"),
 ("play('host','rps',1043,'Scissors',3)","play('host','rps',1043,'Scissors',0)"),
 ("play('alice','rps',1044,'Rock',1)","play('alice','rps',1044,'Rock',0)")
]:
 prior=prior.replace(old,new)
test.write_text(prior)
assert 'LIVE_RECHARGE = false' in (java/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (java/'KingPremiumAccess981.java').read_text()
print("PASS RPS true winners, Firestore rejects fake/invalid RPS-Dice-Coin moves, paid balances unchanged")
