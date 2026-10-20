package com.kingplus.social;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Validates room document IDs in original KING Plus invite text/links.
 * No backend privileges or private-room access are granted by parsing a link.
 */
public final class KingRoomInvite959 {
    private static final String ID = "[A-Za-z0-9_-]{12,80}";
    private static final Pattern LINK = Pattern.compile("kingplus://party/("+ID+")(?![A-Za-z0-9_/-])", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOKEN = Pattern.compile("(?:KINGROOM:|Room\\s+ID:)\\s*("+ID+")(?![A-Za-z0-9_-])", Pattern.CASE_INSENSITIVE);
    private KingRoomInvite959() {}

    public static String parse(String input) {
        if (input == null) return "";
        String s=input.trim();
        if(s.length()==0 || s.length()>2500) return "";
        Matcher m=LINK.matcher(s);
        if(m.find()) return m.group(1);
        m=TOKEN.matcher(s);
        if(m.find()) return m.group(1);
        return s.matches(ID)?s:"";
    }

    public static String link(String id) {
        return id != null && id.matches(ID) ? "kingplus://party/"+id : "";
    }
}
