package com.kingplus.social;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Safe public KING ID sharing for real signed-in, user-to-user connection.
 * No passwords, tokens, Firebase credentials, or database privileges in links.
 */
public final class KingSocialShare992 {
    private KingSocialShare992(){}
    public static final String APP_APK_URL=
        "https://github.com/avaiyakrushal/KING-Plus/releases/download/v9.9.2/KING-Plus-v9.9.2-Follow-Message-Party.apk";
    private static final Pattern ID=Pattern.compile(
        "(?:KING\\s*ID\\s*[:：]\\s*)([0-9]{6})(?![0-9])",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern UID=Pattern.compile(
        "(?:Verified\\s+(?:Firebase\\s+)?UID\\s*[:：]\\s*)([A-Za-z0-9_-]{12,128})(?![A-Za-z0-9_-])",
        Pattern.CASE_INSENSITIVE);
    public static String extractAccount(String raw){
        if(raw==null)return "";
        String s=raw.trim();
        if(s.length()>2500)return "";
        Matcher exact=UID.matcher(s);
        if(exact.find())return exact.group(1);
        Matcher numeric=ID.matcher(s);
        if(numeric.find())return numeric.group(1);
        return s;
    }
    public static String shareKingId(String id,String firebaseUid,String apkUrl){
        if(id==null||!id.matches("[0-9]{6}")||
            firebaseUid==null||!firebaseUid.matches("[A-Za-z0-9_-]{12,128}"))return "";
        return "Join me on KING Plus!\nKING ID: "+id+
             "\nVerified Firebase UID: "+firebaseUid+
             "\nInstall APK: "+(apkUrl==null?APP_APK_URL:apkUrl)+
             "\nOpen People -> Search ID -> Follow -> Message -> Party";
    }
}
