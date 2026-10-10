package com.kingplus.social;

public final class KingConnectionDiagnostics983Test {
    private static int passed;
    private static void eq(boolean got, boolean want,String name) {
        if(got!=want)throw new AssertionError(name+" wanted "+want+" got "+got);
        passed++;
    }
    public static void main(String[] args){
        eq(KingConnectionDiagnostics983.code("123456"),true,"six-digit Room code");
        eq(KingConnectionDiagnostics983.code("12345"),false,"invalid short code");
        eq(KingConnectionDiagnostics983.code("1234567"),false,"not seven-digit");
        eq(KingConnectionDiagnostics983.exactRoom("L4kjh7bnaX8YU32b"),true,"full Room ID");
        eq(KingConnectionDiagnostics983.exactRoom("123456"),false,"numeric code distinguished");
        eq(KingConnectionDiagnostics983.exactRoom("../private"),false,"path traversal rejected");
        eq(KingConnectionDiagnostics983.exactRoom(""),false,"empty id");
        eq(KingConnectionDiagnostics983.safeRoom(" 123456 ").equals("123456"),true,"trimmed code");
        eq(KingConnectionDiagnostics983.safeRoom("https://server/secret").isEmpty(),true,"no URLs");
        eq(KingConnectionDiagnostics983.uidPrefix("abcdefghijkl").equals("abcdef…"),true,"UID redacted");
        eq(KingConnectionDiagnostics983.uidPrefix(null).equals("not signed in"),true,"no leaked uid");
        eq(KingConnectionDiagnostics983.friendlyError("PERMISSION_DENIED").contains("rules"),true,"rules explanation");
        eq(KingConnectionDiagnostics983.friendlyError("UNAVAILABLE").contains("unreachable"),true,"offline");
        eq(KingConnectionDiagnostics983.friendlyError("RESOURCE_EXHAUSTED").contains("quota"),true,"quota");
        eq(KingConnectionDiagnostics983.friendlyError("UNAUTHENTICATED").contains("Sign in"),true,"auth");
        eq(KingConnectionDiagnostics983.safeLine("Error\\r\\nsecret",16).contains("\\n"),false,"sanitize newlines");
        eq(KingConnectionDiagnostics983.active(3,3,"alice","alice",true),true,"current scan");
        eq(KingConnectionDiagnostics983.active(3,4,"alice","alice",true),false,"stale scan");
        eq(KingConnectionDiagnostics983.active(3,3,"alice","bob",true),false,"account switched");
        eq(KingConnectionDiagnostics983.active(3,3,"alice","alice",false),false,"activity closed");
        System.out.println("PASS KING Plus v9.8.3 on-phone Firestore diagnostics: "+passed+" tests");
    }
}
