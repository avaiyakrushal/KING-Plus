package com.kingplus.social;

import android.app.Activity;
import android.graphics.Color;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

/** Trusted grant rendering only. User-owned profile fields never award paid cosmetics. */
public final class KingVipIdentityStyle {
    private KingVipIdentityStyle(){}
    public static boolean validNumber(String v){return v!=null&&v.matches("[1-9][0-9]{5}");}
    public static int color(String key){
        if("gold".equals(key))return 0xffffd54f;
        if("royal_blue".equals(key))return 0xff448aff;
        if("rose".equals(key))return 0xfff06292;
        if("emerald".equals(key))return 0xff40c987;
        if("violet".equals(key))return 0xffb388ff;
        if("cyan".equals(key))return 0xff4dd0e1;
        return 0;
    }
    public static String colorLabel(String key){
        if("gold".equals(key))return "Gold";
        if("royal_blue".equals(key))return "Royal Blue";
        if("rose".equals(key))return "Rose";
        if("emerald".equals(key))return "Emerald";
        if("violet".equals(key))return "Violet";
        if("cyan".equals(key))return "Cyan";
        return "None";
    }
    public static void applyGrant(DocumentSnapshot doc,TextView name,TextView number){
        if(doc==null||!doc.exists()||doc.getString("uid")==null||
            !doc.getId().equals(doc.getString("uid")))return;
        int c=color(doc.getString("nameColor"));
        if(name!=null&&c!=0)name.setTextColor(c);
        String premium=doc.getString("vipNumber");
        if(number!=null&&validNumber(premium))number.setText("👑 VIP ID "+premium+"  ✓");
    }
    public static void load(Activity activity, String uid,TextView name,TextView number){
        if(activity==null||uid==null||uid.isEmpty()||!KingDocumentId10036.valid(uid))return;
        FirebaseFirestore.getInstance().collection("king_vip_identity_grants").document(uid)
            .get(Source.SERVER).addOnSuccessListener(doc->{
                if(activity.isFinishing()||activity.isDestroyed())return;
                // Only apply if this view still belongs to the requested profile.
                if(name!=null&&(!name.isAttachedToWindow()))return;
                if(number!=null&&(!number.isAttachedToWindow()))return;
                applyGrant(doc,name,number);
            });
    }
}
