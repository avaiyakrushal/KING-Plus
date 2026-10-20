package com.kingplus.social;
import java.util.*;

/** Personal bookmarks only; never grants room entry, VIP or wallet privileges. */
public final class KingSavedRoomsPolicy9990 {
    public static final String FIELD="savedPartyRooms9990";
    public static final int MAX_FAVORITES=60, MAX_RECENT=40;
    private KingSavedRoomsPolicy9990(){}
    public static final class Room {
        public final String id,name;
        public final boolean favorite;
        public final long visited;
        Room(String id,String name,boolean favorite,long visited){this.id=id;this.name=name;this.favorite=favorite;this.visited=visited;}
    }
    public static Map<String,Map<String,Object>> clean(Object raw){
        Map<String,Map<String,Object>> out=new LinkedHashMap<>();
        if(!(raw instanceof Map))return out;
        for(Map.Entry<?,?> e:((Map<?,?>)raw).entrySet()){
            if(!(e.getKey() instanceof String)||!(e.getValue() instanceof Map))continue;
            String id=(String)e.getKey();if(!KingMeRoomHistory9981.validId(id)||id.equals(".")||id.equals(".."))continue;
            Map<?,?> v=(Map<?,?>)e.getValue();Object n=v.get("name"),t=v.get("visited");
            String name=n instanceof String?((String)n).trim():"KING Party";
            if(name.isEmpty())name="KING Party";if(name.length()>120)name=name.substring(0,120);
            long at=t instanceof Long?Math.max(0,(Long)t):0;
            out.put(id,record(name,Boolean.TRUE.equals(v.get("favorite")),at));
        }
        return out;
    }
    private static Map<String,Object> record(String name,boolean favorite,long visited){
        Map<String,Object> row=new HashMap<>();row.put("name",name);row.put("favorite",favorite);row.put("visited",visited);return row;
    }
    public static List<Room> rows(Object raw,boolean favoritesOnly){
        List<Room> rows=new ArrayList<>();
        for(Map.Entry<String,Map<String,Object>> e:clean(raw).entrySet()){
            Map<String,Object> v=e.getValue();boolean fav=Boolean.TRUE.equals(v.get("favorite"));long at=(Long)v.get("visited");
            if(favoritesOnly?!fav:at<=0)continue;
            rows.add(new Room(e.getKey(),(String)v.get("name"),fav,at));
        }
        rows.sort((a,b)->{int c=favoritesOnly?a.name.compareToIgnoreCase(b.name):Long.compare(b.visited,a.visited);return c==0?a.id.compareTo(b.id):c;});
        return rows;
    }
    /** Explicit removal from both Favorites and Recent; no fake visit generated. */
    public static Map<String,Map<String,Object>> remove(Object raw,String id){
        if(!KingMeRoomHistory9981.validId(id)||id.equals(".")||id.equals(".."))
            throw new IllegalArgumentException("Invalid room");
        Map<String,Map<String,Object>> result=clean(raw);
        result.remove(id);
        return result;
    }
    public static Map<String,Map<String,Object>> change(Object raw,String id,String name,long at,boolean toggle){
        if(!KingMeRoomHistory9981.validId(id)||id.equals(".")||id.equals(".."))throw new IllegalArgumentException("Invalid room");
        Map<String,Map<String,Object>> all=clean(raw);Map<String,Object> old=all.get(id);
        boolean favorite=old!=null&&Boolean.TRUE.equals(old.get("favorite"));
        if(toggle&&!favorite&&rows(all,true).size()>=MAX_FAVORITES)throw new IllegalStateException("Favorites full. Remove one before adding another.");
        long visited=old==null?0:(Long)old.get("visited");
        if(toggle)favorite=!favorite;else visited=Math.max(visited,Math.max(1,at));
        all.put(id,record(name==null||name.trim().isEmpty()?"KING Party":name.trim(),favorite,visited));
        all=clean(all);
        Set<String> keep=new HashSet<>();int count=0;
        for(Room r:rows(all,false))if(count++<MAX_RECENT)keep.add(r.id);
        count=0;for(Room r:rows(all,true))if(count++<MAX_FAVORITES)keep.add(r.id);
        all.keySet().retainAll(keep);return all;
    }
}
