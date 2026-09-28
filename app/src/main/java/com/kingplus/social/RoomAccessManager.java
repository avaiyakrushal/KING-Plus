package com.kingplus.social;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/** Access settings for public/private/password KING Plus rooms. Passwords are stored as hashes, never plaintext. */
public final class RoomAccessManager {
    public interface Result { void done(boolean ok,String message); }
    public interface AccessResult { void done(boolean allowed,String message); }
    private final FirebaseFirestore db=FirebaseFirestore.getInstance();

    public void configure(String roomId,String ownerUid,boolean privateRoom,String password,Result cb){
        Map<String,Object> v=new HashMap<>();
        v.put("private",privateRoom);
        v.put("passwordHash",password==null||password.isEmpty()?null:sha256(password));
        v.put("updatedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).update(v)
          .addOnSuccessListener(x->cb.done(true,"Room access updated"))
          .addOnFailureListener(e->cb.done(false,e.getMessage()));
    }
    public void check(String roomId,String uid,String password,AccessResult cb){
        db.collection("live_rooms").document(roomId).get().addOnSuccessListener(doc->{
            String owner=doc.getString("ownerUid"); if(uid!=null&&uid.equals(owner)){cb.done(true,"Host");return;}
            Boolean priv=doc.getBoolean("private"); String hash=doc.getString("passwordHash");
            if(Boolean.TRUE.equals(priv)){
                db.collection("live_rooms").document(roomId).collection("allowed_users").document(uid).get().addOnSuccessListener(a->{
                    if(a.exists()) cb.done(true,"Allowed"); else if(hash!=null&&hash.equals(sha256(password==null?"":password))) cb.done(true,"Password accepted"); else cb.done(false,"Private room");
                }).addOnFailureListener(e->cb.done(false,"Access check failed"));
            } else if(hash!=null&&!hash.equals(sha256(password==null?"":password))) cb.done(false,"Password required"); else cb.done(true,"Public room");
        }).addOnFailureListener(e->cb.done(false,e.getMessage()));
    }
    public void allowUser(String roomId,String uid,Result cb){
        Map<String,Object> v=new HashMap<>();v.put("allowed",true);v.put("createdAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("allowed_users").document(uid).set(v).addOnSuccessListener(x->cb.done(true,"User allowed")).addOnFailureListener(e->cb.done(false,e.getMessage()));
    }
    private String sha256(String value){try{byte[] d=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();for(byte x:d)b.append(String.format("%02x",x));return b.toString();}catch(Exception e){return "";}}
}
