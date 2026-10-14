package com.kingplus.social;
import java.util.*;
public final class KingSavedRoomsPolicy9990Test {
 static void ok(boolean v,String s){if(!v)throw new AssertionError(s);}
 public static void main(String[] args){
  Map<String,Map<String,Object>> saved=KingSavedRoomsPolicy9990.change(null,"room1","Music",100,false);
  saved=KingSavedRoomsPolicy9990.change(saved,"room1","Music",999,true);
  ok(KingSavedRoomsPolicy9990.rows(saved,true).size()==1,"favorite saved");
  ok(KingSavedRoomsPolicy9990.rows(saved,false).get(0).visited==100,"favorite does not forge visit");
  saved=KingSavedRoomsPolicy9990.change(saved,"room1","Music",200,false);
  ok(KingSavedRoomsPolicy9990.rows(saved,true).get(0).visited==200,"visit preserves favorite");
  saved=KingSavedRoomsPolicy9990.change(saved,"room1","Music",300,true);
  ok(KingSavedRoomsPolicy9990.rows(saved,true).isEmpty(),"unfavorite");
  ok(KingSavedRoomsPolicy9990.rows(saved,false).size()==1,"unfavorite preserves recent");
  for(int i=0;i<110;i++)saved=KingSavedRoomsPolicy9990.change(saved,"room"+i,"Room "+i,1000+i,false);
  ok(saved.size()==40,"recent bounded");
  ok(KingSavedRoomsPolicy9990.rows(saved,false).get(0).id.equals("room109"),"newest first");
  for(int i=0;i<60;i++)saved=KingSavedRoomsPolicy9990.change(saved,"fav"+i,"Favorite "+i,0,true);
  ok(saved.size()==100,"separate favorites retained beyond recent");
  try{KingSavedRoomsPolicy9990.change(saved,"overflow","Full",0,true);throw new AssertionError("favorite cap bypass");}catch(IllegalStateException expected){}
  try{KingSavedRoomsPolicy9990.change(saved,"a/b","Unsafe",0,true);throw new AssertionError("unsafe path");}catch(IllegalArgumentException expected){}
  ok(KingSavedRoomsPolicy9990.clean("broken").isEmpty(),"malformed state handled");
  Map<String,Object> malformed=new HashMap<>();malformed.put("x",Collections.singletonMap("visited","not-time"));malformed.put("/bad",saved);
  ok(KingSavedRoomsPolicy9990.rows(malformed,false).isEmpty(),"malformed timestamp not recent");
  System.out.println("PASS saved rooms retention, favorite limits, toggles, malformed data and visit chronology");
 }
}
