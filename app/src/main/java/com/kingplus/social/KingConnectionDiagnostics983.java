package com.kingplus.social;

/** Deterministic, privacy-preserving interpretation of on-phone Firebase tests. */
public final class KingConnectionDiagnostics983 {
    private KingConnectionDiagnostics983() {}

    public static boolean code(String input) {
        return input != null && input.matches("[0-9]{6}");
    }

    public static boolean exactRoom(String input) {
        return input != null && input.matches("[A-Za-z0-9_-]{5,128}") && !code(input);
    }

    public static String safeRoom(String input) {
        if(input==null)return "";
        String value=input.trim();
        return code(value)||exactRoom(value)?value:"";
    }

    public static String uidPrefix(String uid) {
        if(uid==null||uid.isEmpty())return "not signed in";
        return uid.substring(0,Math.min(6,uid.length()))+"…";
    }

    public static String friendlyError(String code) {
        if(code==null)return "Firebase returned an unknown network or service error.";
        switch(code.toUpperCase(java.util.Locale.US)){
            case "PERMISSION_DENIED":
                return "Firebase rules rejected this read. Check room privacy, account and rules.";
            case "UNAUTHENTICATED":
                return "Firebase session expired. Sign in again, then retry.";
            case "UNAVAILABLE":
            case "DEADLINE_EXCEEDED":
                return "Firebase is unreachable. Check mobile data/Wi-Fi and retry.";
            case "NOT_FOUND":
                return "The requested Room document no longer exists.";
            case "RESOURCE_EXHAUSTED":
                return "Firebase free-plan quota or server limit has been reached.";
            case "FAILED_PRECONDITION":
                return "The query needs a server index or the Firebase server rejected a precondition.";
            default:
                return "Firebase request failed ("+safeWord(code)+").";
        }
    }

    private static String safeWord(String input){
        return input.replaceAll("[^A-Za-z0-9_]", "").substring(0,Math.min(36,input.replaceAll("[^A-Za-z0-9_]","").length()));
    }

    public static String safeLine(String value,int limit) {
        if(value==null)return "unknown";
        String clean=value.replaceAll("[\\r\\n\\t\\p{Cntrl}]"," ").trim();
        return clean.length()>limit?clean.substring(0,limit):clean;
    }

    public static boolean active(int expected,int actual,String uid,String signedUid,boolean alive){
        return expected>0&&expected==actual&&alive
            &&uid!=null&&!uid.isEmpty()&&uid.equals(signedUid);
    }
}
