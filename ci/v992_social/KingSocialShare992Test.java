package com.kingplus.social;

public final class KingSocialShare992Test {
    private static int passed;
    private static void ok(boolean truth,String name){
        if(!truth)throw new AssertionError(name);
        passed++;
    }
    public static void main(String[] args){
        String uid="cyM6sCh123456789012345";
        String invite=KingSocialShare992.shareKingId("513687",uid,KingSocialShare992.APP_APK_URL);
        ok(invite.contains("KING ID: 513687"),"share ID");
        ok(invite.contains(uid),"share globally unambiguous account UID");
        ok(invite.contains("v9.9.2"),"installer URL");
        ok(KingSocialShare992.extractAccount(invite).equals(uid),"extract verified UID first");
        ok(KingSocialShare992.extractAccount("KING ID: 513687").equals("513687"),"extract ID");
        ok(KingSocialShare992.extractAccount("513687").equals("513687"),"direct ID");
        ok(KingSocialShare992.extractAccount("alice").equals("alice"),"name search");
        ok(KingSocialShare992.extractAccount(null).isEmpty(),"null safe");
        ok(KingSocialShare992.extractAccount("X".repeat(2501)).isEmpty(),"oversized share rejected");
        ok(KingSocialShare992.shareKingId("bad",uid,null).isEmpty(),"invalid ID");
        ok(KingSocialShare992.shareKingId("513687","bad",null).isEmpty(),"invalid UID");
        System.out.println("PASS "+passed+" KING social share/search safety tests");
    }
}
