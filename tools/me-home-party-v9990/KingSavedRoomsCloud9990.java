package com.kingplus.social;
import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.*;

public final class KingSavedRoomsCloud9990 {
    private KingSavedRoomsCloud9990(){}
    public interface Result{void done(Exception error);}
    public static boolean account(String uid){FirebaseUser u=FirebaseAuth.getInstance().getCurrentUser();return u!=null&&u.getUid().equals(uid);}
    public static void change(Context context,FirebaseFirestore db,String uid,String id,String name,boolean toggle,Result result){
        if(db==null||!account(uid)){result.done(new IllegalStateException("Sign in to save rooms"));return;}
        DocumentReference ref=db.collection("users").document(uid);
        db.runTransaction(tx->{
            if(!account(uid))throw new IllegalStateException("Account changed");
            DocumentSnapshot doc=tx.get(ref);
            Map<String,Map<String,Object>> saved=KingSavedRoomsPolicy9990.change(doc.contains(KingSavedRoomsPolicy9990.FIELD)?doc.get(KingSavedRoomsPolicy9990.FIELD):local(context,uid),id,name,System.currentTimeMillis(),toggle);
            Map<String,Object> fields=new HashMap<>();fields.put(KingSavedRoomsPolicy9990.FIELD,saved);
            tx.set(ref,fields,SetOptions.mergeFields(KingSavedRoomsPolicy9990.FIELD));return null;
        }).addOnSuccessListener(v->{if(account(uid))result.done(null);})
          .addOnFailureListener(e->{if(account(uid))result.done(e);});
    }
    public static Object local(Context context,String uid){
        android.content.SharedPreferences p=context.getSharedPreferences("king_party",Context.MODE_PRIVATE);
        List<KingMeRoomHistory9981.Room> rooms=KingMeRoomHistory9981.from(
            p.getStringSet(KingAccountRoomHistory9989.key("favorite_rooms_940",uid),Collections.emptySet()),
            p.getStringSet(KingAccountRoomHistory9989.key("recent_rooms_940",uid),Collections.emptySet()),"all");
        Map<String,Map<String,Object>> out=new HashMap<>();
        for(KingMeRoomHistory9981.Room room:rooms){Map<String,Object> row=new HashMap<>();row.put("name",room.name);row.put("favorite",room.favorite);row.put("visited",room.visited);out.put(room.id,row);}
        return out;
    }
    public static void importLocal(Context context,FirebaseFirestore db,String uid){
        Object seed=local(context,uid);if(((Map<?,?>)seed).isEmpty())return;
        DocumentReference ref=db.collection("users").document(uid);
        db.runTransaction(tx->{
            if(!account(uid))return null;
            DocumentSnapshot doc=tx.get(ref);
            if(!doc.contains(KingSavedRoomsPolicy9990.FIELD))tx.set(ref,Collections.singletonMap(KingSavedRoomsPolicy9990.FIELD,seed),SetOptions.mergeFields(KingSavedRoomsPolicy9990.FIELD));
            return null;
        }).addOnFailureListener(e->android.util.Log.w("KINGPlus","Saved room import unavailable"));
    }
    public static void cache(Context context,String uid,Object raw){
        Set<String> favorites=new HashSet<>(),recent=new HashSet<>();
        for(KingSavedRoomsPolicy9990.Room r:KingSavedRoomsPolicy9990.rows(raw,true))favorites.add(r.id+"\u0001"+r.name.replace("\u0001"," "));
        for(KingSavedRoomsPolicy9990.Room r:KingSavedRoomsPolicy9990.rows(raw,false))recent.add(String.format(Locale.US,"%013d::%s::%s",r.visited,r.id,r.name.replace("::"," ")));
        context.getSharedPreferences("king_party",Context.MODE_PRIVATE).edit()
            .putStringSet(KingAccountRoomHistory9989.key("favorite_rooms_940",uid),favorites)
            .putStringSet(KingAccountRoomHistory9989.key("recent_rooms_940",uid),recent).apply();
    }
}
