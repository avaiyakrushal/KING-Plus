package com.kingplus.social;
public final class KingFirestoreRecovery986Test {
    private static int passed=0;
    private static void yes(boolean ok,String label) {
        if(!ok)throw new AssertionError(label);
        passed++;
    }
    public static void main(String[] args) {
        yes(!KingFirestoreRecovery986.projectMismatch("king-plus-2f365"),"expected Firebase");
        yes(KingFirestoreRecovery986.projectMismatch("other-project"),"mismatch");
        yes(!KingFirestoreRecovery986.projectMismatch(null),"unknown not automatically mismatch");
        String cache="Failed to get document from server. (However, this document does exist in the local cache.)";
        yes(KingFirestoreRecovery986.cacheOnly(cache),"cache recognized");
        yes(!KingFirestoreRecovery986.cacheOnly("PERMISSION_DENIED"),"permission not misclassified");
        yes(KingFirestoreRecovery986.guidance("UNAVAILABLE",cache,"king-plus-2f365")
          .contains("LOCAL CACHE only"),"never call cache a verified Room");
        yes(KingFirestoreRecovery986.guidance("PERMISSION_DENIED",cache,"king-plus-2f365")
          .contains("PERMISSION_DENIED"),"permission takes priority");
        yes(KingFirestoreRecovery986.guidance("UNAUTHENTICATED",null,"king-plus-2f365")
          .contains("Sign in"),"auth");
        yes(KingFirestoreRecovery986.guidance("RESOURCE_EXHAUSTED",null,"king-plus-2f365")
          .contains("quota"),"quota");
        yes(KingFirestoreRecovery986.guidance("UNAVAILABLE",null,"wrong-project")
          .contains("Wrong Firebase project"),"project scope takes priority");
        yes(KingFirestoreRecovery986.guidance("DEADLINE_EXCEEDED",null,"king-plus-2f365")
          .contains("unreachable"),"network timeout");
        System.out.println("PASS KING Plus 986 classified SERVER-only failures: "+passed);
    }
}
