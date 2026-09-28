package com.kingplus.social;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.HashMap;
import java.util.Map;

/** Firestore social graph and direct-message foundation for KING Plus. */
public final class SocialManager {
 public interface Result { void done(boolean ok,String message); }
 private final FirebaseFirestore db=FirebaseFirestore.getInstance();
 public void follow(String me,String target,Result cb){if(me==null||target==null||me.equals(target)){cb.done(false,"Invalid user");return;}Map<String,Object>v=new HashMap<>();v.put("createdAt",FieldValue.serverTimestamp());db.collection("users").document(me).collection("following").document(target).set(v).addOnSuccessListener(x->db.collection("users").document(target).collection("followers").document(me).set(v).addOnSuccessListener(y->cb.done(true,"Following")).addOnFailureListener(e->cb.done(false,e.getMessage()))).addOnFailureListener(e->cb.done(false,e.getMessage()));}
 public void unfollow(String me,String target,Result cb){db.collection("users").document(me).collection("following").document(target).delete().addOnSuccessListener(x->db.collection("users").document(target).collection("followers").document(me).delete().addOnSuccessListener(y->cb.done(true,"Unfollowed")).addOnFailureListener(e->cb.done(false,e.getMessage()))).addOnFailureListener(e->cb.done(false,e.getMessage()));}
 public void addFriend(String me,String target,Result cb){if(me==null||target==null||me.equals(target)){cb.done(false,"Invalid user");return;}Map<String,Object>v=new HashMap<>();v.put("createdAt",FieldValue.serverTimestamp());db.collection("users").document(me).collection("friends").document(target).set(v).addOnSuccessListener(x->db.collection("users").document(target).collection("friends").document(me).set(v).addOnSuccessListener(y->cb.done(true,"Friend added")).addOnFailureListener(e->cb.done(false,e.getMessage()))).addOnFailureListener(e->cb.done(false,e.getMessage()));}
 public void sendMessage(String me,String target,String senderName,String text,Result cb){if(text==null||text.trim().isEmpty()){cb.done(false,"Message is empty");return;}String chatId=me.compareTo(target)<0?me+"_"+target:target+"_"+me;Map<String,Object>v=new HashMap<>();v.put("senderUid",me);v.put("receiverUid",target);v.put("senderName",senderName);v.put("text",text.trim());v.put("createdAt",FieldValue.serverTimestamp());db.collection("direct_chats").document(chatId).collection("messages").add(v).addOnSuccessListener(x->cb.done(true,"Message sent")).addOnFailureListener(e->cb.done(false,e.getMessage()));}
 public ListenerRegistration listenMessages(String me,String target,com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot> listener){String chatId=me.compareTo(target)<0?me+"_"+target:target+"_"+me;return db.collection("direct_chats").document(chatId).collection("messages").orderBy("createdAt",Query.Direction.ASCENDING).limit(100).addSnapshotListener(listener);}
}
