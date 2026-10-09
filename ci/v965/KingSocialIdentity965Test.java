package com.kingplus.social;
public final class KingSocialIdentity965Test {
    private static int total;
    private static void check(boolean ok,String label){if(!ok)throw new AssertionError(label);total++;}
    public static void main(String[] args){
        String alice="myTestRealFirebaseUidABC123";
        String bob="anotherRealGoogleUserUidQ456";
        String publicAlice=KingSocialIdentity965.publicId(alice);
        check(publicAlice.matches("[0-9]{6}"),"six digit KING ID");
        check(publicAlice.equals(String.valueOf((Integer.toUnsignedLong(alice.hashCode())%900000L)+100000L)),"canonical ID matches Main and Social");
        check("000000".equals(KingSocialIdentity965.publicId(null)),"null ID fallback");
        check("000000".equals(KingSocialIdentity965.publicId("")),"blank ID fallback");
        check(KingSocialIdentity965.publicId("Aa").equals(KingSocialIdentity965.publicId("BB")),"6-digit alias not a global unique UID");
        check((alice+"__"+bob).equals(KingSocialIdentity965.followId(alice,bob)),"canonical follow doc ID");
        check((alice+"_"+bob).equals(KingSocialIdentity965.legacyFollowId(alice,bob)),"old follow format recognized");
        check(!KingSocialIdentity965.followId(alice,bob).equals(KingSocialIdentity965.legacyFollowId(alice,bob)),"old/new follow IDs differ");
        check(KingSocialIdentity965.followId(alice,bob).equals(KingSocialIdentity965.followId(alice,bob)),"stable across phones");
        check(!KingSocialIdentity965.followId(alice,bob).equals(KingSocialIdentity965.followId(bob,alice)),"directed follow");
        check(KingSocialIdentity965.validMember(alice,alice),"same Firebase account UID");
        check(!KingSocialIdentity965.validMember(alice,bob),"two users different Firebase UIDs");
        check(!KingSocialIdentity965.validMember(null,bob),"no guest member");
        System.out.println("PASS KING Plus v9.6.5 Social identity: "+total+" tests");
    }
}