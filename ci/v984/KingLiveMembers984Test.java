package com.kingplus.social;
import java.util.Arrays;
import java.util.Collections;

public final class KingLiveMembers984Test {
    private static int checks;
    private static void eq(boolean actual,boolean expected,String name){
        if(actual!=expected)throw new AssertionError(name);checks++;
    }
    public static void main(String[] args){
        eq(KingLiveMembers984.active(1,1,"userA","userA","room1","room1",true),true,"same room account");
        eq(KingLiveMembers984.active(1,1,"userA","userA","room1","room2",true),false,"changed room");
        eq(KingLiveMembers984.active(1,2,"userA","userA","room1","room1",true),false,"outdated test");
        eq(KingLiveMembers984.active(1,1,"userA","userB","room1","room1",true),false,"changed user");
        eq(KingLiveMembers984.active(0,0,"userA","userA","room1","room1",true),false,"generation invalid");
        eq(KingLiveMembers984.active(1,1,null,"userA","room1","room1",true),false,"no user");
        eq(KingLiveMembers984.active(1,1,"userA","userA",null,"room1",true),false,"null Room");
        eq(KingLiveMembers984.active(1,1,"userA","userA","room1","room1",false),false,"screen closed");
        String cached=KingLiveMembers984.state(3,true,true);
        eq(cached.contains("CACHED")&& !cached.contains("LIVE server"),true,"cache is not server proof");
        String live=KingLiveMembers984.state(3,true,false);
        eq(live.contains("LIVE server snapshot"),true,"server verified");
        eq(live.contains("PRESENT"),true,"my member exists");
        eq(KingLiveMembers984.state(0,false,false).contains("NOT PRESENT"),true,"not joined");
        eq(KingLiveMembers984.state(-1,false,false).contains("unavailable"),true,"no snapshot");
        String list=KingLiveMembers984.samplePrefixes(
            Arrays.asList("abcdef123","uvwxyz456","abcdef123"),5);
        eq(list.equals("abcdef…"+", "+"uvwxyz…"),true,"unique redacted members");
        eq(!list.contains("abcdef123"),true,"full UID not leaked");
        eq(KingLiveMembers984.samplePrefixes(Collections.emptyList(),8).equals("none"),true,"empty Room");
        eq(KingLiveMembers984.samplePrefixes(Arrays.asList(null,""),8).equals("none"),true,"invalid UID");
        eq(KingLiveMembers984.samplePrefixes(Arrays.asList("abcdefgh"),0).equals("none"),true,"zero limit");
        eq(KingLiveMembers984.samplePrefixes(Arrays.asList("abcdefgh","qwerty123"),1).equals("abcdef…"),true,"list cap");
        System.out.println("PASS KING Plus v9.8.4 read-only member sync: "+checks+" tests");
    }
}
