package com.kingplus.social;
public final class KingFollowPolicy9997Test {
    public static void main(String[] args) {
        eq(null,KingFollowPolicy9997.key(null,"bob"));
        eq(null,KingFollowPolicy9997.key("alice",null));
        eq(null,KingFollowPolicy9997.key("alice","alice"));
        eq(null,KingFollowPolicy9997.key("alice","a/b"));
        eq(null,KingFollowPolicy9997.key("  ","bob"));
        eq("alice__bob",KingFollowPolicy9997.key("alice","bob"));
        if(!KingFollowPolicy9997.nextFollowing(false)||KingFollowPolicy9997.nextFollowing(true))throw new AssertionError("Toggle broken");
        System.out.println("PASS shared Me/Home/Party follow identity and toggle policy");
    }
    private static void eq(String a,String b) {if(a==null?b!=null:!a.equals(b))throw new AssertionError(a+" != "+b);}
}
