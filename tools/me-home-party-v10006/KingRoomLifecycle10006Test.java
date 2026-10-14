package com.kingplus.social;

public final class KingRoomLifecycle10006Test {
    private static void check(boolean value,String label){if(!value)throw new AssertionError(label);}
    public static void main(String[] ignored){
        KingRoomLifecycle10006 state = new KingRoomLifecycle10006();
        int gift=state.nextBanner();
        check(state.ownsBanner(gift),"gift visible");
        int game=state.nextBanner();
        check(!state.ownsBanner(gift),"old gift timer cannot hide new game");
        check(state.ownsBanner(game),"game visible");
        state.invalidateBanners();
        check(!state.ownsBanner(game),"leave invalidates callback");
        check(KingRoomLifecycle10006.acceptsRoomWrite("roomA","roomA","uidA","uidA",5,5,true),"same room accepts");
        check(!KingRoomLifecycle10006.acceptsRoomWrite("roomA","roomB","uidA","uidA",5,5,true),"new room denies");
        check(!KingRoomLifecycle10006.acceptsRoomWrite("roomA","roomA","uidA","uidB",5,5,true),"other account denies");
        check(!KingRoomLifecycle10006.acceptsRoomWrite("roomA","roomA","uidA","uidA",5,6,true),"new generation denies");
        check(!KingRoomLifecycle10006.acceptsRoomWrite("roomA","roomA","uidA","uidA",5,5,false),"closed view denies");
        System.out.println("PASS v10.0.6 banner epochs and room/account game callback isolation (9 cases)");
    }
}
