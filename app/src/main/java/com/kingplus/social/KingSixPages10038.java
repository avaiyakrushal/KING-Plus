package com.kingplus.social;

/** Validation shared by Me, Home, Party, Discover and Messages. Pure Java for CI regressions. */
public final class KingSixPages10038 {
    private KingSixPages10038() {}

    public static boolean validFamilyCode(String code){
        return code!=null && code.matches("[A-HJ-NP-Z2-9]{6}");
    }

    public static boolean roomOwnerMatches(String expected,String actual){
        if(!KingDocumentId10036.valid(actual))return false;
        return expected==null||expected.isEmpty()||expected.equals(actual);
    }

    public static String profileError(String bio,String hometown,String birthday,String tags){
        if(bio==null||bio.length()>500)return "Bio must be 500 characters or less";
        if(hometown==null||hometown.length()>80)return "Hometown must be 80 characters or less";
        if(birthday==null||birthday.length()>30)return "Birthday must be 30 characters or less";
        if(tags==null||tags.length()>120)return "Labels must be 120 characters or less";
        return null;
    }

    public static boolean exactPrivateThreadMembers(String first,String second,String viewer){
        return KingDocumentId10036.valid(first) && KingDocumentId10036.valid(second)
            && KingDocumentId10036.valid(viewer) && !first.equals(second)
            && (viewer.equals(first)||viewer.equals(second));
    }
}
