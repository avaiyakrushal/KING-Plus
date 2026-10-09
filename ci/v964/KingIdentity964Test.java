package com.kingplus.social;
public final class KingIdentity964Test {
    private static int checks;
    private static void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks++;}
    public static void main(String[] args){
        check(KingIdentity964.sixDigits("231994"),"legacy six digit");
        check(KingIdentity964.joinCode("123456"),"room code");
        check(!KingIdentity964.joinCode("12345"),"short code rejected");
        check(!KingIdentity964.joinCode("1234567"),"long code rejected");
        check(!KingIdentity964.sixDigits("ABC123"),"nonnumeric");
        check(!KingIdentity964.sixDigits(null),"null numeric");
        check(KingIdentity964.firebaseUid("AbCdefGhijKlMnOpQrStUvWxYz12"),"Firebase UID");
        check(KingIdentity964.firebaseUid("uid_2026_QWEght7LmnsOpq"),"UID hyphen/underscore");
        check(!KingIdentity964.firebaseUid("231994"),"legacy code not UID");
        check(!KingIdentity964.firebaseUid("../room_code"),"reject path");
        check(!KingIdentity964.firebaseUid("hello world sample user123456"),"reject spaces");
        System.out.println("PASS KING Plus v9.6.4 Identity test: "+checks+" cases");
    }
}
