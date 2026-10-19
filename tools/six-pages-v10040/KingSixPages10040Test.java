package com.kingplus.social;
public final class KingSixPages10040Test {
  static int n=0;
  static void check(boolean v,String label){n++;if(!v)throw new AssertionError(label);}
  public static void main(String[] args){
    check(KingSixPages10040.sameAccount("abc","abc"),"same account");
    check(!KingSixPages10040.sameAccount("abc","xyz"),"changed account");
    check(!KingSixPages10040.sameAccount("", ""),"blank owner");
    check(!KingSixPages10040.sameAccount(null,null),"null owner");
    check(KingSixPages10040.safePeer("self","peer"),"other peer");
    check(!KingSixPages10040.safePeer("self","self"),"own peer");
    check(!KingSixPages10040.safePeer("self","a/b"),"Firestore document path");
    check(!KingSixPages10040.safePeer("self"," peer"),"whitespace peer");
    check(!KingSixPages10040.safePeer("self",null),"null peer");
    check(KingSixPages10040.canEnterRoom(true,false,"owner","owner"),"live room");
    check(!KingSixPages10040.canEnterRoom(true,true,"owner","owner"),"closed room");
    check(!KingSixPages10040.canEnterRoom(false,false,"owner","owner"),"missing room");
    check(!KingSixPages10040.canEnterRoom(true,false,"owner","wrong"),"changed host");
    check(KingSixPages10040.safePreview(null,50).isEmpty(),"missing preview");
    check(KingSixPages10040.safePreview("Hi\nthere",60).equals("Hi there"),"one line");
    check(KingSixPages10040.safePreview("123456",3).equals("123…"),"bounded preview");
    check(KingSixPages10040.safePreview("x",0).equals("…"),"zero max");
    check(KingSixPages10040.currentGameRound(2,2),"same game");
    check(!KingSixPages10040.currentGameRound(2,3),"stale game callback");
    System.out.println("KING v10.0.40 guards: "+n+" tests PASS");
  }
}
