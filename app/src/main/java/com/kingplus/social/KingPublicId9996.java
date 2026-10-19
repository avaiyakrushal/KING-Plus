package com.kingplus.social;
/** Use only a real six-digit KING ID supplied by public_profiles. */
public final class KingPublicId9996 {
    private KingPublicId9996(){}
    public static String verified(String issued){
        if(issued==null)return null;
        String value=issued.trim();
        return value.matches("[0-9]{6}")?value:null;
    }
}
