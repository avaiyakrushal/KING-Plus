package com.kingplus.social;

public final class KingSixPages10038Test {
    private static int assertions;
    private static void check(boolean value,String why){assertions++;if(!value)throw new AssertionError(why);}
    public static void main(String[] args){
        check(KingSixPages10038.validFamilyCode("ABCDEF"),"letters");
        check(KingSixPages10038.validFamilyCode("A2C3D9"),"letters/numbers");
        for(String bad:new String[]{null,"","ABCDE","ABCDEFG","abcdef","A/CDEF","AAAAA ","OOOOOO","111111","IABCDE","A\\CDEF"})
            check(!KingSixPages10038.validFamilyCode(bad),"invalid family code "+bad);
        check(KingSixPages10038.roomOwnerMatches("host-1","host-1"),"same host");
        check(KingSixPages10038.roomOwnerMatches("","host-1"),"empty expected host");
        check(!KingSixPages10038.roomOwnerMatches("host-1","host-2"),"changed host rejected");
        check(!KingSixPages10038.roomOwnerMatches("host-1",null),"missing host rejected");
        check(!KingSixPages10038.roomOwnerMatches(null," bad"),"invalid account rejected");
        check(KingSixPages10038.profileError("bio","town","2000-01-01","tags")==null,"valid profile");
        check(KingSixPages10038.profileError("a".repeat(500),"b".repeat(80),"c".repeat(30),"d".repeat(120))==null,"max lengths");
        check(KingSixPages10038.profileError("a".repeat(501),"","","")!=null,"oversize bio");
        check(KingSixPages10038.profileError("","a".repeat(81),"","")!=null,"oversize hometown");
        check(KingSixPages10038.profileError("","","a".repeat(31),"")!=null,"oversize birthday");
        check(KingSixPages10038.profileError("","","","a".repeat(121))!=null,"oversize tags");
        check(KingSixPages10038.exactPrivateThreadMembers("me","peer","me"),"valid viewer first");
        check(KingSixPages10038.exactPrivateThreadMembers("peer","me","me"),"valid viewer second");
        for(String bad:new String[]{" me","me ","me/other","","me\\other"})
            check(!KingSixPages10038.exactPrivateThreadMembers(bad,"peer","me"),"noncanonical thread "+bad);
        check(!KingSixPages10038.exactPrivateThreadMembers("me","me","me"),"same member twice");
        check(!KingSixPages10038.exactPrivateThreadMembers("a","b","me"),"viewer not in thread");
        check(!KingSixPages10038.exactPrivateThreadMembers(null,"peer","me"),"null account");
        System.out.println("PASS "+assertions+" six-page account/family/room/profile/thread assertions");
    }
}
