package com.kingplus.social;
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
   return "🏆 "+winner+" beats "+loser+"\n"+String.join(", ",victors);
 }
}
