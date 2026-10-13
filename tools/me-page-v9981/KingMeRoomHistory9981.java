package com.kingplus.social;
import java.util.*;
/** Shows PartyActivity's real Favorite/Recent records instead of fake Me rooms. */
public final class KingMeRoomHistory9981 {
 private KingMeRoomHistory9981(){}
 public static class Room {
  public final String id,name; public final boolean favorite;public final long visited;
  Room(String id,String name,boolean favorite,long visited){this.id=id;this.name=name;this.favorite=favorite;this.visited=visited;}
  public String label(){return (favorite?"★ ":"🕘 ")+name;}
 }
 public static boolean validId(String id){
  if(id==null||id.isEmpty()||id.length()>500)return false;
  for(int i=0;i<id.length();i++)if(id.charAt(i)=='/'||id.charAt(i)<32)return false;
  return !id.trim().isEmpty();
 }
 public static List<Room> from(Set<String> favorites,Set<String> recent,String tab){
  Map<String,Room> all=new LinkedHashMap<>();
  if(favorites!=null)for(String raw:favorites) {
   if(raw==null)continue;int at=raw.indexOf('\u0001');
   if(at<=0)continue;String id=raw.substring(0,at),name=raw.substring(at+1);
   if(validId(id))all.put(id,new Room(id,name.isEmpty()?"KING Party":name,true,0));
  }
  List<Room> history=new ArrayList<>();
  if(recent!=null)for(String raw:recent){
   if(raw==null)continue;String[] values=raw.split("::",3);
   if(values.length!=3||!validId(values[1]))continue;
   try {long at=Long.parseLong(values[0]);if(at<0)continue;
    history.add(new Room(values[1],values[2].isEmpty()?"KING Party":values[2],false,at));
   }catch(NumberFormatException ignored){}
  }
  history.sort((a,b)->Long.compare(b.visited,a.visited));
  for(Room r:history)all.putIfAbsent(r.id,r);
  List<Room> result=new ArrayList<>();
  for(Room r:all.values())if("favorites".equals(tab)?r.favorite:("recent".equals(tab)?!r.favorite:true))result.add(r);
  if("recent".equals(tab))result.sort((a,b)->Long.compare(b.visited,a.visited));
  else result.sort((a,b)->a.name.compareToIgnoreCase(b.name));
  return result.size()>25?new ArrayList<>(result.subList(0,25)):result;
 }
}
