package com.kingplus.social;
import java.util.*;
public final class KingSocialRealtime970Test {
    private static int checks;
    private static void eq(boolean found,boolean expected,String why){
        if(found!=expected)throw new AssertionError(why+" expected "+expected);
        checks++;
    }
    public static void main(String[] args){
        eq(KingSocialRealtime970.activeAccount("alice","alice",true),true,"signed-in account");
        eq(KingSocialRealtime970.activeAccount("alice","bob",true),false,"different logged in account");
        eq(KingSocialRealtime970.activeAccount("alice",null,true),false,"signed out");
        eq(KingSocialRealtime970.activeAccount(null,"alice",true),false,"missing expected user");
        eq(KingSocialRealtime970.activeAccount("alice","alice",false),false,"destroyed screen");
        eq(KingSocialRealtime970.isCurrentView("search","search",4,4,true),true,"latest search");
        eq(KingSocialRealtime970.isCurrentView("search","discover",4,4,true),false,"changed tab");
        eq(KingSocialRealtime970.isCurrentView("search","search",3,4,true),false,"out of order reply");
        eq(KingSocialRealtime970.isCurrentView("search","search",0,0,true),false,"invalid generation");
        eq(KingSocialRealtime970.isCurrentView("search","search",4,4,false),false,"closed screen");
        eq(KingSocialRealtime970.sameQuery("123456","123456"),true,"numeric id");
        eq(KingSocialRealtime970.sameQuery("123456","654321"),false,"outdated input");
        eq(KingSocialRealtime970.sameQuery(" uid ","uid"),true,"trimmed exact UID");
        eq(KingSocialRealtime970.showUid("alice","bob"),true,"other member");
        eq(KingSocialRealtime970.showUid("alice","alice"),false,"hide self");
        eq(KingSocialRealtime970.showUid(null,"alice"),false,"hide null uid");
        Set<String> f=KingSocialRealtime970.friends(Arrays.asList("alice","bob","bob",null),
            Arrays.asList("bob","charlie","bob"));
        eq(f.size()==1&&f.contains("bob"),true,"mutual follow only once");
        eq(KingSocialRealtime970.friends(null,Arrays.asList("a")).isEmpty(),true,"null outgoing");
        eq(KingSocialRealtime970.friends(Arrays.asList("a"),null).isEmpty(),true,"null incoming");
        System.out.println("KING Plus v9.7.0 Realtime Social: "+checks+" tests PASS");
    }
}
