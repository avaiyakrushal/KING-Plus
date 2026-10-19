package com.kingplus.social;

/** A single Firestore document ID, not a path. Never trim into another identity. */
public final class KingDocumentId10036 {
    private KingDocumentId10036() {}
    public static boolean valid(String value) {
        if(value!=null)for(int i=0;i<value.length();i++)
            if(Character.isISOControl(value.charAt(i)))return false;
        return value != null && !value.isEmpty() && value.equals(value.trim())
            && value.indexOf('/') < 0 && value.indexOf('\\') < 0
            && !value.equals(".") && !value.equals("..")
            && value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 1500;
    }
}
