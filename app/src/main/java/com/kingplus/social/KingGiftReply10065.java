package com.kingplus.social;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
/** A success boolean from an old endpoint is not a receipt for this purchase. */
public final class KingGiftReply10065 {
    private KingGiftReply10065(){}
    public static String giftId(String uid,String request){
        try{
            byte[] hash=MessageDigest.getInstance("SHA-256").digest((uid+'\0'+request).getBytes(StandardCharsets.UTF_8));
            StringBuilder result=new StringBuilder("gift_");
            for(byte value:hash){int n=value&255;result.append(Character.forDigit(n>>>4,16)).append(Character.forDigit(n&15,16));}
            return result.toString();
        }catch(java.security.NoSuchAlgorithmException error){throw new IllegalStateException(error);}
    }
    public static boolean confirmed(Object raw,String uid,String request){
        if(!(raw instanceof Map)||uid==null||request==null)return false;
        Map<?,?> data=(Map<?,?>)raw;
        if(!Boolean.TRUE.equals(data.get("ok"))||!giftId(uid,request).equals(data.get("giftId")))return false;
        Object value=data.get("balance");if(!(value instanceof Number))return false;
        double balance=((Number)value).doubleValue();
        return !Double.isNaN(balance)&&!Double.isInfinite(balance)&&balance>=0&&balance<=1000000000L&&balance==Math.floor(balance);
    }
}
