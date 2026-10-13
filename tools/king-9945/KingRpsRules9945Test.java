package com.kingplus.social;
import java.util.*;
public final class KingRpsRules9945Test{
 static void check(boolean c,String s){if(!c)throw new AssertionError(s);}
 static String result(String a,String b){
   return KingRpsRules9945.result(Arrays.asList("Alice","Bob"),Arrays.asList(a,b));
 }
 public static void main(String[] args) {
   check(KingRpsRules9945.valid("Rock"),"Rock valid");
   check(KingRpsRules9945.valid("Paper"),"Paper valid");
   check(KingRpsRules9945.valid("Scissors"),"Scissors valid");
   check(!KingRpsRules9945.valid("Dragon"),"fake choice blocked");
   check(!KingRpsRules9945.valid("rock"),"case-sensitive rule");
   check(result("Rock","Scissors").contains("Alice"),"Rock beats Scissors");
   check(result("Scissors","Rock").contains("Bob"),"Rock beats Scissors reverse");
   check(result("Paper","Rock").contains("Alice"),"Paper beats Rock");
   check(result("Rock","Paper").contains("Bob"),"Paper beats Rock reverse");
   check(result("Scissors","Paper").contains("Alice"),"Scissors beats Paper");
   check(result("Paper","Scissors").contains("Bob"),"Scissors beats Paper reverse");
   check(result("Rock","Rock").contains("draw"),"same move draws");
   check(result("Paper","Paper").contains("draw"),"same paper draws");
   check(result("Scissors","Scissors").contains("draw"),"same scissors draws");
   check(KingRpsRules9945.result(Arrays.asList("A","B","C"),Arrays.asList("Rock","Paper","Scissors")).contains("draw"),"3 choice draw");
   check(result("Rock","Dragon").contains("Invalid"),"unknown move never wins");
   check(KingRpsRules9945.result(Arrays.asList("Alice"),Arrays.asList("Rock")).contains("Waiting"),"must have 2 players");
   check(KingRpsRules9945.result(Arrays.asList("Alice","Bob"),Arrays.asList("Rock","Paper")).contains("Bob"),"correct remote winner");
   System.out.println("PASS actual RPS: 6 permutations, 3 draws, multiple players, invalid, 2-player min");
 }
}