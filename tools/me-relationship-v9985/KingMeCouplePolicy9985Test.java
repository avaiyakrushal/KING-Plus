package com.kingplus.social;
import java.util.*;
public final class KingMeCouplePolicy9985Test {
 static void yes(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  String a="firstuser123",b="seconduser456";
  yes(KingMeCouplePolicy9985.pairId(a,b).equals(KingMeCouplePolicy9985.pairId(b,a)),"canonical pair");
  yes(!KingMeCouplePolicy9985.validUid("invalid/x"),"no path injection");
  boolean threw=false;try{KingMeCouplePolicy9985.pairId(a,a);}catch(IllegalArgumentException ok){threw=true;}
  yes(threw,"no self relationship");
  yes(!KingMeCouplePolicy9985.canAccept(a,b,a,"pending"),"initiator cannot self-accept");
  yes(KingMeCouplePolicy9985.canAccept(a,b,b,"pending"),"receiver can accept");
  yes(!KingMeCouplePolicy9985.canAccept(a,b,b,"accepted"),"cannot re-accept settled");
  List<String[]> rows=Arrays.asList(new String[]{a,b,"accepted"},new String[]{b,a,"accepted"},
      new String[]{a,"thirduser123","pending"},new String[]{"eviluser333","otheruser123","accepted"});
  yes(KingMeCouplePolicy9985.countAccepted(a,rows)==1,"duplicate pair counts once; pending ignored");
  yes(KingMeCouplePolicy9985.countAccepted("notinpair123",rows)==0,"no fabricated CP");
  System.out.println("PASS canonical Relationship pair, reciprocal consent, signed-in actual accepted CP only");
 }
}
