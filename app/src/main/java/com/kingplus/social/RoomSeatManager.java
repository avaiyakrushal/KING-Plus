package com.kingplus.social;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

/** Firestore-backed room seat/control state for KING Plus voice rooms. */
public final class RoomSeatManager {
    public interface Result { void done(boolean ok, String message); }
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ListenerRegistration listen(String roomId, com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot> listener) {
        return db.collection("live_rooms").document(roomId).collection("seats").orderBy("index").addSnapshotListener(listener);
    }
    public ListenerRegistration listenRequests(String roomId, com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot> listener) {
        return db.collection("live_rooms").document(roomId).collection("seat_requests").addSnapshotListener(listener);
    }
    public void requestSeat(String roomId,String uid,String name,Result cb) {
        Map<String,Object> v=new HashMap<>(); v.put("uid",uid); v.put("name",name); v.put("status","requested"); v.put("createdAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("seat_requests").document(uid).set(v).addOnSuccessListener(x->cb.done(true,"Seat requested")).addOnFailureListener(e->cb.done(false,e.getMessage()));
    }
    public void approveRequest(String roomId,String requestUid,String requestName,int index,Result cb) {
        if(index<1 || index>8){ cb.done(false,"Seat must be 1-8"); return; }
        setSeat(roomId,index,requestUid,requestName,false,(ok,msg)->{
            if(!ok){ cb.done(false,msg); return; }
            db.collection("live_rooms").document(roomId).collection("seat_requests").document(requestUid).delete()
              .addOnSuccessListener(x->cb.done(true,"Approved to seat " + index)).addOnFailureListener(e->cb.done(false,e.getMessage()));
        });
    }
    public void rejectRequest(String roomId,String requestUid,Result cb) {
        db.collection("live_rooms").document(roomId).collection("seat_requests").document(requestUid).delete()
          .addOnSuccessListener(x->cb.done(true,"Seat request rejected")).addOnFailureListener(e->cb.done(false,e.getMessage()));
    }
    public void setSeat(String roomId,int index,String uid,String name,boolean muted,Result cb) {
        if(index<1 || index>8){ cb.done(false,"Seat must be 1-8"); return; }
        Map<String,Object> v=new HashMap<>(); v.put("index",index); v.put("uid",uid); v.put("name",name); v.put("muted",muted); v.put("updatedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(index)).set(v).addOnSuccessListener(x->cb.done(true,"Seat updated")).addOnFailureListener(e->cb.done(false,e.getMessage()));
    }
    public void leaveSeat(String roomId,int index,Result cb) { db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(index)).delete().addOnSuccessListener(x->cb.done(true,"Seat left")).addOnFailureListener(e->cb.done(false,e.getMessage())); }
    public void lockSeat(String roomId,int index,boolean locked,Result cb) {
        if(index<1 || index>8){ cb.done(false,"Seat must be 1-8"); return; }
        Map<String,Object> v=new HashMap<>(); v.put("index",index); v.put("locked",locked); v.put("updatedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("seat_locks").document(String.valueOf(index)).set(v).addOnSuccessListener(x->cb.done(true,locked?"Seat locked":"Seat unlocked")).addOnFailureListener(e->cb.done(false,e.getMessage()));
    }
    public void setRole(String roomId,String uid,String role,Result cb) { Map<String,Object> v=new HashMap<>(); v.put("role",role); v.put("updatedAt",FieldValue.serverTimestamp()); db.collection("live_rooms").document(roomId).collection("roles").document(uid).set(v).addOnSuccessListener(x->cb.done(true,"Role updated")).addOnFailureListener(e->cb.done(false,e.getMessage())); }
    public void setMuted(String roomId,int index,boolean muted,Result cb) { db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(index)).update("muted",muted,"updatedAt",FieldValue.serverTimestamp()).addOnSuccessListener(x->cb.done(true,muted?"Muted":"Unmuted")).addOnFailureListener(e->cb.done(false,e.getMessage())); }
    public void banFromRoom(String roomId,String uid,String reason,Result cb) { Map<String,Object> v=new HashMap<>(); v.put("reason",reason==null?"Host action":reason); v.put("createdAt",FieldValue.serverTimestamp()); db.collection("live_rooms").document(roomId).collection("bans").document(uid).set(v).addOnSuccessListener(x->cb.done(true,"User banned from room")).addOnFailureListener(e->cb.done(false,e.getMessage())); }
}
