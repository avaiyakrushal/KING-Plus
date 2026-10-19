package com.kingplus.social;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import java.net.URL;
import java.util.function.Consumer;

/** KING original media entry. App membership is verified on the server first.
 * Public Jitsi rooms are still test-only, NOT authenticated production RTC.
 */
public final class NativeMeetBridge {
    private NativeMeetBridge() {}

    public static void verifyMembership(Activity activity, String rawId, Consumer<Boolean> result) {
        if(activity==null||activity.isFinishing()||activity.isDestroyed())return;
        final String id=rawId==null?"":rawId.trim();
        final boolean isFamily=id.startsWith("FAMILY-");
        final String document=isFamily?id.substring(7):id;
        if(!document.matches("[A-Za-z0-9_-]{5,128}")||
             "king-public".equalsIgnoreCase(id)){
            deny(activity,"Join a real KING Plus Party or Family first.",result);return;
        }
        final FirebaseUser user;
        final FirebaseFirestore db;
        try {
            user=FirebaseAuth.getInstance().getCurrentUser();
            db=FirebaseFirestore.getInstance();
        } catch(RuntimeException e) {
            deny(activity,"Firebase login could not be verified.",result);return;
        }
        if(user==null||user.getUid()==null||user.getUid().isEmpty()){
            deny(activity,"Sign in to KING Plus before opening voice/video.",result);return;
        }
        final String uid=user.getUid(),collection=isFamily?"families":"live_rooms";
        db.collection(collection).document(document).get(Source.SERVER)
          .addOnSuccessListener(room->{
            if(!currentAccount10065(activity,uid))return;
            if(!room.exists()||Boolean.TRUE.equals(room.getBoolean("closed"))){
                deny(activity,"The live room or Family has closed.",result);return;
            }
            db.collection(collection).document(document).collection("members").document(uid)
              .get(Source.SERVER).addOnSuccessListener(member->{
                if(!currentAccount10065(activity,uid))return;
                String memberUid=member.getString("uid");
                boolean authorized=member.exists() &&
                    (isFamily?(memberUid==null||uid.equals(memberUid)):uid.equals(memberUid));
                if(!authorized){deny(activity,"Join this Party or Family before calling.",result);return;}
                result.accept(true);
              }).addOnFailureListener(error->
                deny(activity,"Membership could not be verified online.",result));
          }).addOnFailureListener(error->
            deny(activity,"Room could not be verified online.",result));
    }

    private static boolean currentAccount10065(Activity activity,String uid){
        FirebaseUser now=FirebaseAuth.getInstance().getCurrentUser();
        return !activity.isFinishing()&&!activity.isDestroyed()&&now!=null&&uid.equals(now.getUid());
    }
    private static void deny(Activity activity,String message,Consumer<Boolean> result){
        if(activity.isFinishing()||activity.isDestroyed())return;
        Toast.makeText(activity,message,Toast.LENGTH_LONG).show();
        result.accept(false);
    }

    public static boolean launch(Activity activity,String roomId,String roomName,
                                 String displayName,boolean video){
        if(activity==null||activity.isFinishing()||activity.isDestroyed())return false;
        verifyMembership(activity,roomId,allowed->{
            if(allowed&&!activity.isFinishing()&&!activity.isDestroyed())
                startVerified(activity,roomId,roomName,displayName,video);
        });
        return true; // verification pending, NOT proof media is connected
    }

    private static void startVerified(Activity activity,String roomId,String roomName,
                                      String displayName,boolean video) {
        try{
            String slug=("KINGPlus-"+roomId).replaceAll("[^A-Za-z0-9_-]","");
            JitsiMeetUserInfo info=new JitsiMeetUserInfo();
            info.setDisplayName(displayName==null||displayName.trim().isEmpty()?
                "KING User":displayName.trim());
            JitsiMeetConferenceOptions options=new JitsiMeetConferenceOptions.Builder()
                .setServerURL(new URL("https://meet.jit.si"))
                .setRoom(slug)
                .setSubject(roomName==null||roomName.trim().isEmpty()?
                    "KING Plus Party":roomName)
                .setAudioMuted(true).setVideoMuted(true)
                .setUserInfo(info)
                .setFeatureFlag("welcomepage.enabled",false)
                .setFeatureFlag("prejoinpage.enabled",true)
                .setFeatureFlag("pip.enabled",true)
                .build();
            JitsiMeetActivity.launch(activity,options);
        }catch(Throwable error){
            try{
                Toast.makeText(activity,"Media unavailable. Opening confirmed compatibility mode.",
                    Toast.LENGTH_SHORT).show();
                Intent i=new Intent(activity,VoiceWebActivity.class);
                i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);
                i.putExtra("displayName",displayName);i.putExtra("video",video);
                activity.startActivity(i);
            }catch(Throwable second){
                KingStability.nonFatal(activity,"rtc-launch-9926",second);
            }
        }
    }
}