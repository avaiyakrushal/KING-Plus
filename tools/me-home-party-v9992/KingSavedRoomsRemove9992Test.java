package com.kingplus.social;
import java.util.*;
public final class KingSavedRoomsRemove9992Test {
  private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
  public static void main(String[] a){
    Map<String,Map<String,Object>> all=KingSavedRoomsPolicy9990.change(null,"alpha","Alpha",100,false);
    all=KingSavedRoomsPolicy9990.change(all,"alpha","Alpha",100,true);
    all=KingSavedRoomsPolicy9990.change(all,"beta","Beta",200,false);
    all=KingSavedRoomsPolicy9990.change(all,"gamma","Gamma",300,true);
    Map<String,Map<String,Object>> after=KingSavedRoomsPolicy9990.remove(all,"alpha");
    require(all.containsKey("alpha"),"Removal mutated input");
    require(!after.containsKey("alpha"),"Removed item still present");
    require(after.containsKey("beta")&&after.containsKey("gamma"),"Other saved rooms altered");
    require(KingSavedRoomsPolicy9990.rows(after,true).size()==1,"Unrelated favorite missing");
    require(KingSavedRoomsPolicy9990.rows(after,false).size()==1,"Unrelated recent missing");
    require(KingSavedRoomsPolicy9990.rows(after,false).get(0).visited==200,"Recent time rewritten");
    after=KingSavedRoomsPolicy9990.remove(after,"beta");
    require(!after.containsKey("beta"),"Recent removal failed");
    require(KingSavedRoomsPolicy9990.rows(after,true).size()==1,"Favorite erased while removing recent");
    after=KingSavedRoomsPolicy9990.remove(after,"gamma");
    require(after.isEmpty(),"Final saved room removal failed");
    after=KingSavedRoomsPolicy9990.remove(after,"gamma");
    require(after.isEmpty(),"Repeat removal not idempotent");
    for(String invalid:new String[]{"", "a/b",".",".."}){
       try{KingSavedRoomsPolicy9990.remove(after,invalid);throw new AssertionError("Unsafe ID allowed: "+invalid);}
       catch(IllegalArgumentException expected){}
    }
    require(KingSavedRoomsPolicy9990.remove("garbage","alpha").isEmpty(),"Corrupt data not sanitized");
    System.out.println("PASS remove favorite/recent only, preserve others, idempotent, invalid ID rejected");
  }
}
