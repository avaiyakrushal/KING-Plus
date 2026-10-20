package com.kingplus.social;
public final class KingDocumentId10036Test {
    public static void main(String[] args){
        String[] invalid={null,""," "," room","room ","a/b","a/b/c",".","..","a\nb","a\tb","x".repeat(1501),"હ".repeat(501)};
        for(String s:invalid)if(KingDocumentId10036.valid(s))throw new AssertionError("Accepted invalid ID: "+s);
        String[] valid={"room-123","firebase_UID_29","123456","હ".repeat(500),"x".repeat(1500)};
        for(String s:valid)if(!KingDocumentId10036.valid(s))throw new AssertionError("Rejected valid ID");
        System.out.println("PASS 18 invalid/valid document ID boundary cases");
    }
}
