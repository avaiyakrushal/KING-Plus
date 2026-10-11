package com.kingplus.social;
/** Run with javac/java in GitHub Actions; no phone needed for pure chess rules. */
public final class KingChessRulesTest {
 static String board=KingChessRules.START,rights="KQkq",status="active";
 static char turn='w';static int ep=-1;
 static void reset(){board=KingChessRules.START;rights="KQkq";status="active";turn='w';ep=-1;}
 static void move(String f,String t){
   KingChessRules.Result r=KingChessRules.play(board,turn,rights,ep,KingChessRules.square(f),KingChessRules.square(t));
   board=r.board;rights=r.rights;ep=r.enPassant;turn=r.next;status=r.status;
 }
 static void illegal(String f,String t){
   try{move(f,t);throw new AssertionError("Illegal move allowed: "+f+t);}
   catch(IllegalArgumentException expected){}
 }
 public static void main(String[] args){
   if(board.length()!=64)throw new AssertionError("Initial board invalid");
   illegal("e2","e5");illegal("e7","e5");
   move("e2","e4");move("e7","e5");move("g1","f3");move("b8","c6");
   if(!"active".equals(status))throw new AssertionError("opening not active");
   reset();move("e2","e4");move("a7","a6");move("e4","e5");move("d7","d5");move("e5","d6");
   if(board.charAt(KingChessRules.square("d5"))!='.'||board.charAt(KingChessRules.square("d6"))!='P')
     throw new AssertionError("En passant failed");
   reset();move("e2","e4");move("e7","e5");move("d1","h5");move("b8","c6");
   move("f1","c4");move("g8","f6");move("h5","f7");
   if(!"checkmate".equals(status))throw new AssertionError("Scholar's mate: "+status);
   reset();move("f2","f3");move("e7","e5");move("g2","g4");move("d8","h4");
   if(!"checkmate".equals(status))throw new AssertionError("Fool's mate: "+status);
   reset();move("e2","e4");move("e7","e5");move("g1","f3");move("b8","c6");
   move("f1","e2");move("g8","f6");move("e1","g1");
   if(board.charAt(KingChessRules.square("g1"))!='K'||board.charAt(KingChessRules.square("f1"))!='R')
     throw new AssertionError("King-side castling failed");
   System.out.println("PASS chess: legal moves, turns, en passant, castling and checkmates");
 }
}