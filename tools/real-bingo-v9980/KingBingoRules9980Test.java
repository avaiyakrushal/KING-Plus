package com.kingplus.social;
import java.util.*;
public final class KingBingoRules9980Test {
 static void check(boolean b,String msg){if(!b)throw new AssertionError(msg);}
 public static void main(String[] args){
  for(int seed=0;seed<250;seed++) {
    List<Integer> card=KingBingoRules9980.newCard(new Random(seed));
    check(KingBingoRules9980.validCard(card),"B/I/N/G/O column range, unique and center "+seed);
    check(new HashSet<>(card).size()==25,"unique 24 numbers plus zero "+seed);
  }
  List<Integer> card=KingBingoRules9980.newCard(new Random(77));
  List<Integer> calls=new ArrayList<>(Arrays.asList(0));
  for(int col=0;col<4;col++) calls.add(card.get(col));
  check(!KingBingoRules9980.isWinningLine(card,calls),"4/5 is never a Bingo");
  calls.add(card.get(4));
  check(KingBingoRules9980.isWinningLine(card,calls),"complete first row Bingo");
  check(KingBingoRules9980.remaining(calls).size()==70,"75 balls minus 5 called");
  List<Integer> other=new ArrayList<>(card);other.set(0,77);
  check(!KingBingoRules9980.validCard(other),"out-of-range number denied");
  other=new ArrayList<>(card);other.set(0,other.get(5));
  check(!KingBingoRules9980.validCard(other),"duplicate card value denied");
  List<Integer> invalidCalls=Arrays.asList(0,4,4);
  check(!KingBingoRules9980.validCalls(invalidCalls),"duplicate called number denied");
  List<Integer> diagonal=new ArrayList<>(Arrays.asList(0));
  for(int i=0;i<5;i++)if(i!=2) diagonal.add(card.get(i*5+i));
  check(KingBingoRules9980.isWinningLine(card,diagonal),"free-center diagonal Bingo");
  List<Integer> incomplete=new ArrayList<>(diagonal);incomplete.remove(1);
  check(!KingBingoRules9980.isWinningLine(card,incomplete),"incomplete diagonal denied");
  System.out.println("PASS 250 random 5x5 Bingo cards and authentic row/column/free-center diagonal rules");
 }
}
