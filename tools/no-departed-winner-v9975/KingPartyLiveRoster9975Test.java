package com.kingplus.social;
import java.util.*;
public final class KingPartyLiveRoster9975Test {
 static void check(boolean yes,String message){if(!yes)throw new AssertionError(message);}
 public static void main(String[]args){
   List<String> roster=Arrays.asList("alice","bob","cara","dan");
   check(KingPartyLiveRoster9975.allPresent(roster,roster),"Four current members may finish");
   check(!KingPartyLiveRoster9975.allPresent(roster,Arrays.asList("alice","bob","cara")),"Dan left AFTER his move: no 4/4 winner");
   check(!KingPartyLiveRoster9975.allPresent(roster,Arrays.asList("alice","bob","cara","eve")),"Eve cannot replace Dan");
   check(!KingPartyLiveRoster9975.allPresent(Arrays.asList("alice","bob","bob"),roster),"Duplicate roster rejected");
   check(!KingPartyLiveRoster9975.allPresent(Arrays.asList("alice"),roster),"Single user rejected");
   check(KingPartyLiveRoster9975.member("bob",true,"bob"),"Live member valid");
   check(!KingPartyLiveRoster9975.member("bob",false,"bob"),"Deleted member doc blocks result");
   check(!KingPartyLiveRoster9975.member("bob",true,"eve"),"Forged UID blocks result");
   check(!KingPartyLiveRoster9975.member("bob",true,null),"Null UID blocks result");
   System.out.println("PASS KING Party membership verified even AFTER all moves; no departed player winner");
 }
}
