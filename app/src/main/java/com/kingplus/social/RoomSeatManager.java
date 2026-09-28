package com.kingplus.social;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.HashMap;
import java.util.Map;

public final class RoomSeatManager {
    public interface Result { void done(boolean ok,String message); }
    private final FirebaseFirestore db=FirebaseFirestore.getInstance();
    public ListenerRegistration listen(String r,com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot> l){return db.collection("live_rooms").document(r).collection("seats").orderBy("index").addSnapshotListener(l);}
    public ListenerRegistration listenRequests(String r,com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot> l){return db.collection("live_rooms").document(r).collection("seat_requests").addSnapshotListener(l);}
    public void requestSeat(String r,String u,String n,Result cb){Map<String,Object>v=new HashMap<>();v.put("uid",u);v.put("name",n);v.put("status","requested");v.put("createdAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(r).collection("seat_requests").document(u).set(v).addOnSuccessListener(x->cb.done(true,"Seat requested")).addOnFailureListener(e->cb.done(false,e.getMessage()));}
    public void approveRequest(String r,String u,String n,int i,Result cb){if(i<1||i>8){cb.done(false,"Seat must be 1-8");return;}setSeatInternal(r,i,u,n,false,cb,true);}
    public void rejectRequest(String r,String u,Result cb){db.collection("live_rooms").document(r).collection("seat_requests").document(u).delete().addOnSuccessListener(x->cb.done(true,"Seat request rejected")).addOnFailureListener(e->cb.done(false,e.getMessage()));}
    public void setSeat(String r,int i,String u,String n,boolean m,Result cb){if(i<1||i>8){cb.done(false,"Seat must be 1-8");return;}db.collection("live_rooms").document(r).collection("seat_locks").document(String.valueOf(i)).get().addOnSuccessListener(lock->{if(Boolean.TRUE.equals(lock.getBoolean("locked"))){cb.done(false,"Seat "+i+" is locked. Ask host to unlock it.");return;}setSeatInternal(r,i,u,n,m,cb,false);}).addOnFailureListener(e->cb.done(false,"Could not check seat lock"));}
    private void setSeatInternal(String r,int i,String u,String n,boolean m,Result cb,boolean approved){Map<String,Object>v=new HashMap<>();v.put("index",i);v.put("uid",u);v.put("name",n);v.put("muted",m);v.put("updatedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(r).collection("seats").document(String.valueOf(i)).set(v).addOnSuccessListener(x->{if(approved)db.collection("live_rooms").document(r).collection("seat_requests").document(u).delete().addOnSuccessListener(y->cb.done(true,"Approved to seat "+i)).addOnFailureListener(e->cb.done(false,e.getMessage()));else cb.done(true,"Joined seat "+i);}).addOnFailureListener(e->cb.done(false,e.getMessage()));}
    public void leaveSeat(String r,int i,Result cb){db.collection("live_rooms").document(r).collection("seats").document(String.valueOf(i)).delete().addOnSuccessListener(x->cb.done(true,"Seat left")).addOnFailureListener(e->cb.done(false,e.getMessage()));}
    public void lockSeat(String r,int i,boolean l,Result cb){if(i<1||i>8){cb.done(false,"Seat must be 1-8");return;}Map<String,Object>v=new HashMap<>();v.put("index",i);v.put("locked",l);v.put("updatedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(r).collection("seat_locks").document(String.valueOf(i)).set(v).addOnSuccessListener(x->cb.done(true,l?"Seat locked":"Seat unlocked")).addOnFailureListener(e->cb.done(false,e.getMessage()));}
    public void setRole(String r,String u,String role,Result cb){Map<String,Object>v=new HashMap<>();v.put("role",role);v.put("updatedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(r).collection("roles").document(u).set(v).addOnSuccessListener(x->cb.done(true,"Role updated")).addOnFailureListener(e->cb.done(false,e.getMessage()));}
    public void setMuted(String r,int i,boolean m,Result cb){db.collection("live_rooms").document(r).collection("seats").document(String.valueOf(i)).update("muted",m,"updatedAt",FieldValue.serverTimestamp()).addOnSuccessListener(x->cb.done(true,m?"Muted":"Unmuted")).addOnFailureListener(e->cb.done(false,e.getMessage()));}
    public void banFromRoom(String r,String u,String reason,Result cb){Map<String,Object>v=new HashMap<>();v.put("reason",reason==null?"Host action":reason);v.put("createdAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(r).collection("bans").document(u).set(v).addOnSuccessListener(x->cb.done(true,"User banned from room")).addOnFailureListener(e->cb.done(false,e.getMessage()));}
}
