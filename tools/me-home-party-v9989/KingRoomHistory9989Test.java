package com.kingplus.social;
import java.util.*;
public final class KingRoomHistory9989Test {
    static void check(boolean v,String message){if(!v)throw new AssertionError(message);}
    public static void main(String[] args){
        Set<String> favorites=new HashSet<>(Arrays.asList("a\u0001Old name","b\u0001Unvisited"));
        Set<String> recent=new HashSet<>(Arrays.asList("100::a::Older","300::a::Latest","200::c::Music","invalid","100::/bad::Bad"));
        List<KingMeRoomHistory9981.Room> rooms=KingMeRoomHistory9981.from(favorites,recent,"recent");
        check(rooms.size()==2,"Recent contains favorite and normal visits, no duplicates or malformed rows");
        check(rooms.get(0).id.equals("a")&&rooms.get(0).visited==300,"Most recent favorite stays first");
        check(rooms.get(0).favorite&&rooms.get(0).name.equals("Latest"),"Favorite flag and latest name survive merge");
        check(rooms.get(1).id.equals("c"),"Other visited room retained");
        check(KingMeRoomHistory9981.from(favorites,recent,"favorites").size()==2,"Unvisited favorite remains in Favorites only");
        String alice=KingAccountRoomHistory9989.key("favorite_rooms_940","alice");
        check(!alice.equals(KingAccountRoomHistory9989.key("favorite_rooms_940","bob")),"Two accounts never share history key");
        check(!alice.equals(KingAccountRoomHistory9989.key("recent_rooms_940","alice")),"Favorite and recent namespaces differ");
        check(!KingAccountRoomHistory9989.key("favorite_rooms_940",null).equals(KingAccountRoomHistory9989.key("favorite_rooms_940","guest")),"Guest cannot collide with signed account");
        check(!alice.equals("favorite_rooms_940"),"Unowned legacy records not exposed");
        check(KingMeRoomHistory9981.from(Collections.emptySet(),Collections.emptySet(),"recent").isEmpty(),"Empty stays empty");
        System.out.println("PASS 10 Me and Party account history regressions");
    }
}
