package com.kingplus.social;

/** Classifies on-phone Firestore SERVER failures without trusting cached Room data. */
public final class KingFirestoreRecovery986 {
    private KingFirestoreRecovery986() {}
    public static final String EXPECTED_PROJECT = "king-plus-2f365";

    public static boolean projectMismatch(String id) {
        return id != null && !id.trim().isEmpty() &&
            !EXPECTED_PROJECT.equals(id.trim());
    }

    public static boolean cacheOnly(String message) {
        if (message == null) return false;
        String m=message.toLowerCase(java.util.Locale.US);
        return m.contains("local cache") || m.contains("cached document")
            || m.contains("does exist in the local cache");
    }

    public static String guidance(String code, String message, String project) {
        if(projectMismatch(project)) {
            return "Wrong Firebase project in this APK. Expected " + EXPECTED_PROJECT
                + "; detected " + safeProject(project) + ". Rebuild with the correct config.";
        }
        String c=code==null ? "" : code.toUpperCase(java.util.Locale.US);
        if(c.equals("PERMISSION_DENIED"))
            return "Firestore PERMISSION_DENIED. Check deployed Room Rules, "
                + "account membership and private-room access. Do not bypass the server.";
        if(c.equals("UNAUTHENTICATED"))
            return "Firebase sign-in expired. Sign in again, then test the SAME Room.";
        if(c.equals("RESOURCE_EXHAUSTED"))
            return "Firebase quota or backend rate limit may be exhausted. "
                + "Check project usage before retrying.";
        if(c.equals("NOT_FOUND"))
            return "Room document was not found on the Firebase server. "
                + "Have the host share a new full Room invitation.";
        if(cacheOnly(message))
            return "Room data exists in LOCAL CACHE only. The Firebase SERVER "
                + "did NOT confirm the Room. Cached access is unsafe; do not join "
                + "or change seats offline. Run Test Firebase / Party connection "
                + "with mobile data, then copy the test report.";
        if(c.equals("UNAVAILABLE") || c.equals("DEADLINE_EXCEEDED"))
            return "Firestore SERVER is unreachable or timed out. "
                + "Try mobile data or disable VPN/Private DNS, then run the "
                + "Firebase / Party connection test. Never treat a cache hit as a join.";
        return "The Firestore SERVER did not verify the Room. "
            + "Use Test Firebase / Party connection and copy its diagnostic report.";
    }

    private static String safeProject(String id){
        String cleaned=id==null?"unknown":id.replaceAll("[^A-Za-z0-9_-]","");
        return cleaned.length()>48 ? cleaned.substring(0,48) : cleaned;
    }
}
