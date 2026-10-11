package com.kingplus.social;
public final class KingFreshHostRoom991Test {
    private static int passed;
    private static void yes(boolean ok,String title){
        if(!ok)throw new AssertionError(title);
        passed++;
    }
    public static void main(String[] args){
        yes(KingFreshHostRoom991.canWriteWithoutPreRead("r1","r1","host","host",true),"fresh host");
        yes(!KingFreshHostRoom991.canWriteWithoutPreRead("r1","r2","host","host",true),"switched room");
        yes(!KingFreshHostRoom991.canWriteWithoutPreRead(null,"r1","host","host",true),"no proof");
        yes(!KingFreshHostRoom991.canWriteWithoutPreRead("r1","r1","host","guest",true),"guest");
        yes(!KingFreshHostRoom991.canWriteWithoutPreRead("r1","r1","host","host",false),"owner mismatch");
        yes(!KingFreshHostRoom991.canWriteWithoutPreRead("","", "host","host",true),"empty room");
        yes(!KingFreshHostRoom991.canWriteWithoutPreRead("r1","r1",null,null,true),"not signed in");
        System.out.println("PASS KING fresh host fast join "+passed+" guard tests");
    }
}
