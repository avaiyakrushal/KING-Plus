#!/usr/bin/env python3
from pathlib import Path
import sys
root=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
m=root/'MainActivity.java';s=m.read_text()
old='''        firestore.collection("public_profiles").document(me.getUid()).set(profile, com.google.firebase.firestore.SetOptions.merge());'''
new='''        firestore.collection("public_profiles").document(me.getUid())
            .set(profile,com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener(v->getSharedPreferences("king_social_sync_996",MODE_PRIVATE)
                .edit().putString("uid",me.getUid()).putString("state","SYNCED")
                .putString("error","").putLong("confirmedAt",System.currentTimeMillis()).apply())
            .addOnFailureListener(error->getSharedPreferences("king_social_sync_996",MODE_PRIVATE)
                .edit().putString("uid",me.getUid()).putString("state","FAILED")
                .putString("error",String.valueOf(error.getLocalizedMessage())).apply());'''
assert s.count(old)==1
s=s.replace(old,new,1)
old='''    @Override protected void onResume(){
        super.onResume();
        if("profile".equals(screen)'''
new='''    private long lastProfileAttempt996;
    @Override protected void onResume(){
        super.onResume();
        if(firestore!=null&&firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null){
            long now=android.os.SystemClock.elapsedRealtime();
            if(lastProfileAttempt996==0||now-lastProfileAttempt996>120000L){
                lastProfileAttempt996=now;
                syncPublicProfile();
            }
        }
        if("profile".equals(screen)'''
assert s.count(old)==1
m.write_text(s.replace(old,new,1))
print("PASS Main: auto-register profile when signed in, actual cloud ack, retry")
