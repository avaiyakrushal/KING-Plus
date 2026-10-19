package com.kingplus.social;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Real Firestore transaction for Follow/Unfollow on Home, Me and Party. No local fake success. */
public final class KingFollowLive9997 {
    private static final Set<String> IN_FLIGHT = new HashSet<>();
    private KingFollowLive9997(){}
    public interface Callback { void finished(Boolean following, Exception error); }
    private static synchronized boolean enter(String key){return IN_FLIGHT.add(key);}
    private static synchronized void exit(String key){IN_FLIGHT.remove(key);}

    public static void toggle(FirebaseFirestore db, FirebaseUser me, String targetUid,
                              String followerName, Callback callback){
        change(db,me,targetUid,followerName,false,callback);
    }
    public static void ensureFollowing(FirebaseFirestore db, FirebaseUser me, String targetUid,
                                       String followerName, Callback callback){
        change(db,me,targetUid,followerName,true,callback);
    }
    private static void change(FirebaseFirestore db, FirebaseUser me, String targetUid,
                               String followerName, boolean ensure, Callback callback){
        final String follower=me==null?null:me.getUid();
        final String key=KingFollowPolicy9997.key(follower,targetUid);
        if(db==null||key==null){
            if(callback!=null)callback.finished(null,new IllegalArgumentException("Sign in and choose another KING account"));
            return;
        }
        if(!enter(key)){
            if(callback!=null)callback.finished(null,new IllegalStateException("Please wait for the current Follow request"));
            return;
        }
        final DocumentReference ref=db.collection("follows").document(key);
        try{
            db.runTransaction(tx->{
                DocumentSnapshot previous=tx.get(ref);
                boolean following=previous.exists();
                if(ensure&&following)return true;
                boolean next=ensure||KingFollowPolicy9997.nextFollowing(following);
                if(next){
                    Map<String,Object> record=new HashMap<>();
                    record.put("followerUid",follower);
                    record.put("targetUid",targetUid);
                    record.put("followerName",followerName==null?"KING User":followerName);
                    record.put("createdAt",FieldValue.serverTimestamp());
                    tx.set(ref,record);
                }else tx.delete(ref);
                return next;
            }).addOnCompleteListener(task->{
                exit(key);
                if(callback!=null){
                    if(task.isSuccessful())callback.finished(task.getResult(),null);
                    else callback.finished(null,task.getException()==null?
                        new IllegalStateException("Follow request failed"):task.getException());
                }
            });
        }catch(Exception e){exit(key);if(callback!=null)callback.finished(null,e);}
    }
}
