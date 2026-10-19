package com.kingplus.social;

import android.app.Activity;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import java.util.HashMap;
import java.util.Map;

/** Durable retry key: a lost callable response cannot charge the same tap twice. */
public final class KingGiftPurchase10064 {
    private static final java.util.Set<String> ACTIVE=new java.util.HashSet<>();
    private KingGiftPurchase10064(){}
    public static void retryPending(Activity screen,CloudBackend.Callback done){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){done.onResult(false,"Sign in first.");return;}
        String raw=screen.getSharedPreferences("king_gift_purchase_10064",0).getString("pending_"+user.getUid()+"_payload",null);
        if(raw==null){done.onResult(true,"No pending gift.");return;}
        String[] fields=raw.split("\\|",-1);
        if(fields.length!=5){done.onResult(false,"Pending gift details are unavailable.");return;}
        try{send(screen,fields[0].isEmpty()?null:fields[0],fields[1],fields[2],Integer.parseInt(fields[3]),Integer.parseInt(fields[4]),done);}
        catch(NumberFormatException error){done.onResult(false,"Pending gift details are invalid.");}
    }
    public static void send(Activity screen,String room,String target,String gift,int qty,int total,CloudBackend.Callback done){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user==null||user.isAnonymous()){done.onResult(false,"Sign in to send Diamond gifts.");return;}
        final String uid=user.getUid();
        final SharedPreferences journal=screen.getSharedPreferences("king_gift_purchase_10064",0);
        final String key="pending_"+uid;
        final String payload=(room==null?"":room)+"|"+target+"|"+gift+"|"+qty+"|"+total;
        if(ACTIVE.contains(uid)){done.onResult(false,"Your gift is still being confirmed.");return;}
        String old=journal.getString(key+"_payload",null);
        if(old!=null&&!payload.equals(old)){
            done.onResult(false,"Retry your previous gift first: "+journal.getString(key+"_label","pending gift"));return;
        }
        String token=journal.getString(key,null);
        if(token==null){
            token=java.util.UUID.randomUUID().toString();
            if(!journal.edit().putString(key,token).putString(key+"_payload",payload)
                .putString(key+"_label",gift+" x"+qty).commit()){
                done.onResult(false,"Gift could not be saved for safe retry.");return;
            }
        }
        final String requestId=token;
        ACTIVE.add(uid);
        Map<String,Object> data=new HashMap<>();
        data.put("requestId",requestId);data.put("targetUid",target);
        data.put("giftName",gift+" x"+qty);data.put("cost",total);
        if(room!=null)data.put("roomId",room);
        FirebaseFunctions.getInstance().getHttpsCallable(room==null?"sendGift":"sendRoomGift").call(data)
            .addOnSuccessListener(result->{
                ACTIVE.remove(uid);
                Object raw=result.getData();
                boolean ok=KingGiftReply10065.confirmed(raw,uid,requestId);
                if(ok&&requestId.equals(journal.getString(key,null)))
                    journal.edit().remove(key).remove(key+"_payload").remove(key+"_label").commit();
                if(current(screen,uid))done.onResult(ok,ok?"Gift confirmed • "+total+" Diamonds":"Gift not confirmed. Retry the same gift.");
            }).addOnFailureListener(error->{
                ACTIVE.remove(uid);
                if(error instanceof FirebaseFunctionsException){
                    FirebaseFunctionsException.Code code=((FirebaseFunctionsException)error).getCode();
                    // These errors are returned before any transaction commit.
                    if(code==FirebaseFunctionsException.Code.INVALID_ARGUMENT||code==FirebaseFunctionsException.Code.PERMISSION_DENIED||
                       code==FirebaseFunctionsException.Code.FAILED_PRECONDITION||code==FirebaseFunctionsException.Code.UNAUTHENTICATED||
                       code==FirebaseFunctionsException.Code.NOT_FOUND){
                        if(requestId.equals(journal.getString(key,null)))
                            journal.edit().remove(key).remove(key+"_payload").remove(key+"_label").commit();
                    }
                }
                if(current(screen,uid))done.onResult(false,"Gift confirmation: "+(error.getMessage()==null?"retry the same gift":error.getMessage()));
            });
    }
    private static boolean current(Activity screen,String uid){
        FirebaseUser now=FirebaseAuth.getInstance().getCurrentUser();
        return !screen.isFinishing()&&!screen.isDestroyed()&&now!=null&&uid.equals(now.getUid());
    }
}
