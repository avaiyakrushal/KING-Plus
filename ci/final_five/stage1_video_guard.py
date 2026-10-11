#!/usr/bin/env python3
"""Stage 1 / video gate: never start a Jitsi conference until Firebase server
confirms this authenticated account is an active member of the requested room.
Keep camera and microphone initially OFF. No server JWT is claimed: the existing
meet.jit.si conference still requires trusted RTC hosting for private production.
"""
from pathlib import Path
import sys
pkg=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
f=pkg/'KingMultiVideoActivity.java'
s=f.read_text()
start=s.index('        try{\n            KingCrashWatch958.mark(this,"video-jitsi-init");')
end=s.index('\n    private TextView control(',start)
original=s[start:end]
assert 'meetView.join(options)' in original
assert 'setAudioMuted(true)' in original
assert 'setVideoMuted(true)' in original
assert 'setContentView(shell);attachRealtime940();renderSeats940();refreshControls940();' in original
jitsi=original[:original.index('        setContentView(shell);')]
# Existing try/catch relocated without altering the original conference options.
old=original
replacement='''        setContentView(shell);
        checkVideoRoomAccess998(stage,loading);
    }

    private void checkVideoRoomAccess998(FrameLayout stage,TextView loading){
        if(db==null||me==null||roomId.isEmpty()){
            loading.setText("Sign in and join a real Firebase Party before Multi Video");
            return;
        }
        final String uid998=me.getUid();
        final String candidateRoom998=roomId;
        loading.setText("Checking your Party membership on Firebase SERVER…");
        db.collection("live_rooms").document(candidateRoom998)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(room998->{
              if(isFinishing()||isDestroyed())return;
              if(!room998.exists()||Boolean.TRUE.equals(room998.getBoolean("closed"))){
                  loading.setText("That KING Party is closed or no longer exists");
                  return;
              }
              db.collection("live_rooms").document(candidateRoom998)
                .collection("members").document(uid998)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(member998->{
                    if(isFinishing()||isDestroyed())return;
                    if(!member998.exists()
                        ||!uid998.equals(member998.getString("uid"))){
                        loading.setText("Join this Party Room on your account before using Multi Video");
                        return;
                    }
                    startVerifiedVideo998(stage,loading);
                })
                .addOnFailureListener(e->{
                    if(!isFinishing()&&!isDestroyed())
                        loading.setText("Room member permission denied / offline: "+
                          String.valueOf(e.getLocalizedMessage()));
                });
          })
          .addOnFailureListener(e->{
              if(!isFinishing()&&!isDestroyed())
                  loading.setText("Could not verify KING Party on server: "+
                    String.valueOf(e.getLocalizedMessage()));
          });
    }

    private void startVerifiedVideo998(FrameLayout stage,TextView loading){
        if(isFinishing()||isDestroyed()||meetView!=null)return;
''' + jitsi + '''        attachRealtime940();
        renderSeats940();
        refreshControls940();
    }
'''
assert s.count(old)==1
s=s.replace(old,replacement,1)
assert s.count('meetView.join(options)')==1
assert s.count('attachRealtime940();')==1
assert 'get(com.google.firebase.firestore.Source.SERVER)' in s
f.write_text(s)
print('PASS only authenticated verified room members join Jitsi video from KING app')
print('PASS camera/mic muted by default, existing media controls and RTC lifecycle retained')
print('IMPORTANT: meet.jit.si is an outside provider; production private-room JWT/RTC and physical device tests are not complete')
