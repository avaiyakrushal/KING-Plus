package com.kingplus.social;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KingMultiVideoActivity extends androidx.fragment.app.FragmentActivity implements org.jitsi.meet.sdk.JitsiMeetActivityInterface {
    private org.jitsi.meet.sdk.JitsiMeetView meetView;
    private String roomId="",roomName="KING Plus Multi Video",displayName="KING User";
    private FirebaseFirestore db; private FirebaseUser me;
    private ListenerRegistration eventsListener,roomListener,roleListener,videoSeatListener9921,videoInviteListener9921,selfMemberListener9921,videoMatchIncoming9922,videoMatchOutgoing9922;
    private boolean serverVerified9921=false, videoTransportStarted9921=false, videoActionBusy9921=false;
    private String currentMatchTarget9922="";
    private final Map<Integer,String> seatUids=new HashMap<>(),seatNames=new HashMap<>();
    private final Map<Integer,Boolean> seatMics=new HashMap<>(),seatCameras=new HashMap<>();
    private final Set<String> handledInvites940=new HashSet<>(),handledMatches940=new HashSet<>(),handledMatchRequests9922=new HashSet<>();
    private int mySeat=-1; private boolean micOn=false,cameraOn=false,moderator=false,owner940=false,cohost940=false,leaving940=false;
    private LinearLayout seatsBar,controls; private TextView stateText,seatAction,micAction,camAction,matchAction;
    private final Handler heartbeat940=new Handler(Looper.getMainLooper());
    // Firestore document is the authoritative seat; historical events never claim it.
    private final Runnable heartbeatTask940=new Runnable(){@Override public void run(){
        if(mySeat>0&&!leaving940&&serverVerified9921&&videoTransportStarted9921){
            final int n=mySeat; final String owner=me==null?"":me.getUid();
            if(db!=null&&!owner.isEmpty()){
                final DocumentReference seat=videoSeat9921(n);
                db.runTransaction(tx->{DocumentSnapshot d=tx.get(seat);
                    if(d.exists()&&owner.equals(d.getString("uid")))
                        tx.update(seat,"updatedAt",FieldValue.serverTimestamp());
                    return null;
                }).addOnFailureListener(e->KingStability.nonFatal(KingMultiVideoActivity.this,"video-heartbeat-9921",e));
            }
            heartbeat940.postDelayed(this,25000L);
        }
    }};

    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(null,Typeface.BOLD);return t;}
    private String safe(String s,String f){return s==null||s.trim().isEmpty()?f:s.trim();}
    private void toast(String s){if(!isFinishing())android.widget.Toast.makeText(this,s,android.widget.Toast.LENGTH_SHORT).show();}
    private CollectionReference events940(){return db.collection("live_rooms").document(roomId).collection("events");}

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        roomId=safe(getIntent().getStringExtra("roomId"),"");roomName=safe(getIntent().getStringExtra("roomName"),"KING Plus Multi Video");displayName=safe(getIntent().getStringExtra("displayName"),"KING User");
        try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}

        FrameLayout shell=new FrameLayout(this);shell.setBackgroundColor(0xff0d0b15);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);shell.addView(body,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(10),0,dp(12),0);head.setBackgroundColor(0xff171322);
        TextView back=tv("‹",38,Color.WHITE,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->leaveAndFinish940());head.addView(back,new LinearLayout.LayoutParams(dp(50),dp(58)));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);TextView title=tv(roomName,17,Color.WHITE,true);stateText=tv("📹 Multi Video • viewer mode",11,0xffc7bdd5,true);info.addView(title,new LinearLayout.LayoutParams(-1,dp(30)));info.addView(stateText,new LinearLayout.LayoutParams(-1,dp(22)));head.addView(info,new LinearLayout.LayoutParams(0,dp(58),1));
        TextView leave=tv("Leave",12,Color.WHITE,true);leave.setGravity(Gravity.CENTER);leave.setBackground(bg(0xff7b3fd0,18));leave.setOnClickListener(v->leaveAndFinish940());head.addView(leave,new LinearLayout.LayoutParams(dp(72),dp(38)));body.addView(head,new LinearLayout.LayoutParams(-1,dp(62)));

        FrameLayout stage=new FrameLayout(this);body.addView(stage,new LinearLayout.LayoutParams(-1,0,1));TextView loading=tv("Connecting Multi Video…",14,0xffcfc8da,true);loading.setGravity(Gravity.CENTER);stage.addView(loading,new FrameLayout.LayoutParams(-1,-1));
        seatsBar=new LinearLayout(this);seatsBar.setGravity(Gravity.CENTER);seatsBar.setPadding(dp(8),dp(5),dp(8),dp(3));seatsBar.setBackgroundColor(0xff171322);body.addView(seatsBar,new LinearLayout.LayoutParams(-1,dp(68)));
        controls=new LinearLayout(this);controls.setGravity(Gravity.CENTER);controls.setPadding(dp(6),dp(3),dp(6),dp(8));controls.setBackgroundColor(0xff171322);
        seatAction=control("＋ Sit Down",()->toggleSeat940());micAction=control("🎤 Mic OFF",()->toggleMic940());camAction=control("📷 Camera OFF",()->toggleCamera940());matchAction=control("💞 Match",()->videoQuickMatch940());
        controls.addView(seatAction,controlLp());controls.addView(micAction,controlLp());controls.addView(camAction,controlLp());controls.addView(matchAction,controlLp());body.addView(controls,new LinearLayout.LayoutParams(-1,dp(62)));

        setContentView(shell);
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
                    serverVerified9921=true;
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
        try{
            KingCrashWatch958.mark(this,"video-jitsi-init");
            org.jitsi.meet.sdk.JitsiMeet.instantiateReactNative(this);meetView=new org.jitsi.meet.sdk.JitsiMeetView(this);stage.addView(meetView,new FrameLayout.LayoutParams(-1,-1));
            String slug=("KINGPlus-"+roomId).replaceAll("[^A-Za-z0-9_-]","");org.jitsi.meet.sdk.JitsiMeetUserInfo ui=new org.jitsi.meet.sdk.JitsiMeetUserInfo();ui.setDisplayName(displayName);
            org.jitsi.meet.sdk.JitsiMeetConferenceOptions options=new org.jitsi.meet.sdk.JitsiMeetConferenceOptions.Builder().setServerURL(new java.net.URL("https://meet.jit.si")).setRoom(slug).setSubject(roomName).setAudioMuted(true).setVideoMuted(true).setUserInfo(ui)
                .setFeatureFlag("welcomepage.enabled",false).setFeatureFlag("prejoinpage.enabled",false).setFeatureFlag("invite.enabled",false).setFeatureFlag("chat.enabled",false).setFeatureFlag("recording.enabled",false).setFeatureFlag("live-streaming.enabled",false).setFeatureFlag("pip.enabled",true).build();
            meetView.join(options);loading.setVisibility(View.GONE);
            videoTransportStarted9921=true;
        }catch(Throwable e){
            videoTransportStarted9921=false;
            loading.setVisibility(View.VISIBLE);
            loading.setText("Multi Video engine unavailable • return to Party and retry");
            try{if(meetView!=null){meetView.dispose();meetView=null;}}catch(Throwable ignored){}
            KingStability.nonFatal(this,"video-start-9921",e);
            return;
        }
        attachRealtime940();
        renderSeats940();
        refreshControls940();
    }

    private TextView control(String text,Runnable action){TextView v=tv(text,12,Color.WHITE,true);v.setGravity(Gravity.CENTER);v.setBackground(bg(0xff372b4c,15));v.setOnClickListener(x->KingSafe.run(this,"ui-action",action));return v;}
    private LinearLayout.LayoutParams controlLp(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(48),1);p.setMargins(dp(4),0,dp(4),0);return p;}

    private DocumentReference videoSeat9921(int seat){
        return db.collection("live_rooms").document(roomId).collection("video_seats")
            .document(String.valueOf(seat));
    }
    private DocumentReference videoSeatOwner9921(String uid){
        return db.collection("live_rooms").document(roomId).collection("video_member_seats").document(uid);
    }
    private boolean videoReady9921(){
        return db!=null&&me!=null&&!roomId.isEmpty()&&serverVerified9921
            &&videoTransportStarted9921&&!leaving940;
    }
    private void attachRealtime940(){
        if(!videoReady9921()){stateText.setText("📹 Multi Video • join verified Party first");return;}
        final String uid=me.getUid();
        final DocumentReference room=db.collection("live_rooms").document(roomId);
        roomListener=room.addSnapshotListener((doc,e)->{
            if(leaving940||isFinishing()||isDestroyed())return;
            if(e==null&&doc!=null&&doc.exists()&&Boolean.TRUE.equals(doc.getBoolean("closed"))){
                toast("Party was closed");leaveAndFinish940();return;
            }
            owner940=e==null&&doc!=null&&doc.exists()&&uid.equals(doc.getString("ownerUid"));
            refreshModerator940();
        });
        roleListener=room.collection("roles").document(uid).addSnapshotListener((doc,e)->{
            if(leaving940||isFinishing()||isDestroyed())return;
            cohost940=e==null&&doc!=null&&doc.exists()&&"cohost".equals(doc.getString("role"));
            refreshModerator940();
        });
        selfMemberListener9921=room.collection("members").document(uid).addSnapshotListener((doc,e)->{
            if(leaving940||isFinishing()||isDestroyed())return;
            if(e==null&&doc!=null&&!doc.exists()){
                toast("Removed from Party");leaveAndFinish940();
            }
        });
        videoSeatListener9921=room.collection("video_seats").addSnapshotListener((snap,e)->{
            if(leaving940||isFinishing()||isDestroyed())return;
            if(e!=null){stateText.setText("Video seats unavailable: "+e.getMessage());return;}
            seatUids.clear();seatNames.clear();seatMics.clear();seatCameras.clear();
            if(snap!=null)for(DocumentSnapshot doc:snap.getDocuments()){
                int n;try{n=Integer.parseInt(doc.getId());}catch(Exception ignored){continue;}
                if(n<1||n>6)continue;
                String seatUid=safe(doc.getString("uid"),"");if(seatUid.isEmpty())continue;
                seatUids.put(n,seatUid);
                seatNames.put(n,safe(doc.getString("name"),"KING member"));
                seatMics.put(n,Boolean.TRUE.equals(doc.getBoolean("micOn")));
                seatCameras.put(n,Boolean.TRUE.equals(doc.getBoolean("cameraOn")));
            }
            mySeat=-1;micOn=false;cameraOn=false;
            for(Map.Entry<Integer,String> entry:seatUids.entrySet())if(uid.equals(entry.getValue())){
                mySeat=entry.getKey();micOn=Boolean.TRUE.equals(seatMics.get(mySeat));
                cameraOn=Boolean.TRUE.equals(seatCameras.get(mySeat));break;
            }
            heartbeat940.removeCallbacks(heartbeatTask940);
            if(mySeat>0)heartbeat940.postDelayed(heartbeatTask940,25000L);
            applyMedia940();renderSeats940();refreshControls940();
        });
        videoInviteListener9921=room.collection("video_seat_invites").document(uid)
            .addSnapshotListener((doc,e)->{
                if(leaving940||isFinishing()||isDestroyed()||e!=null||doc==null||!doc.exists())return;
                if(mySeat<1&&handledInvites940.add(doc.getId()+":"+String.valueOf(doc.get("createdAt"))))showInvite940(doc);
            });
        // A historical event is never a confirmed match. Only the receiving
        // participant can accept a fresh Firebase video match request.
        videoMatchIncoming9922=room.collection("video_match_requests").document(uid)
            .addSnapshotListener((snap,e)->{
                if(leaving940||isFinishing()||isDestroyed()||e!=null||snap==null||!snap.exists())return;
                if(!"pending".equals(snap.getString("status")))return;
                Timestamp created=snap.getTimestamp("createdAt");
                if(created==null||Math.abs(System.currentTimeMillis()-created.toDate().getTime())>120000L)return;
                String key=snap.getId()+":"+created.toDate().getTime();
                if(!handledMatchRequests9922.add(key))return;
                showVideoMatchRequest9922(snap);
            });
    }

    private void refreshModerator940(){moderator=owner940||cohost940;refreshControls940();}

    // Match notifications now come only from consented Firebase invitations,
    // not replayed/unverifiable room history events.

    private void removeUid940(String uid){for(Integer seat:new ArrayList<>(seatUids.keySet()))if(uid.equals(seatUids.get(seat)))clearSeat940(seat);}
    private void clearSeat940(int seat){seatUids.remove(seat);seatNames.remove(seat);seatMics.remove(seat);seatCameras.remove(seat);}

    private void showInvite940(DocumentSnapshot invite){
        Long raw=invite.getLong("seatNo");int seat=raw==null?-1:raw.intValue();
        if(seat<1||seat>6)return;
        String inviter=safe(invite.getString("actorName"),"Host");
        new android.app.AlertDialog.Builder(this).setTitle("📹 Video seat invitation")
            .setMessage(inviter+" invited you to video seat "+seat+".")
            .setPositiveButton("Sit Down",(d,w)->{
                takeSeat940(seat);
                invite.getReference().delete().addOnFailureListener(e->
                    KingStability.nonFatal(this,"video-invite-accept-9921",e));
            }).setNegativeButton("Reject",(d,w)->{
                invite.getReference().delete().addOnFailureListener(e->
                    KingStability.nonFatal(this,"video-invite-reject-9921",e));
            }).show();
    }

    private void renderSeats940(){
        if(seatsBar==null)return;seatsBar.removeAllViews();
        for(int i=1;i<=6;i++){final int seat=i;String uid=seatUids.get(i),name=seatNames.get(i);boolean mine=me!=null&&me.getUid().equals(uid);
            TextView v=tv(uid==null?"＋":(mine?"●":safe(name,"G").substring(0,1).toUpperCase()),uid==null?18:14,mine?0xffffe500:Color.WHITE,true);v.setGravity(Gravity.CENTER);v.setBackground(bg(mine?0xff51431b:(uid==null?0xff30283b:0xff47345e),24));v.setContentDescription(uid==null?"Empty video seat "+i:"Video seat "+i+" "+name);v.setOnClickListener(x->seatTap940(seat));v.setOnLongClickListener(x->{
                if(!moderator)return false;
                if(uid==null)inviteToVideoSeat940(seat);
                else new android.app.AlertDialog.Builder(this).setTitle(safe(name,"KING member"))
                    .setMessage("Remove this user from video seat "+seat+"?")
                    .setPositiveButton("Remove",(d,w)->removeSeat940(seat))
                    .setNegativeButton("Cancel",null).show();
                return true;
            });
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(50),1);p.setMargins(dp(3),0,dp(3),0);seatsBar.addView(v,p);}
    }
    private void seatTap940(int seat){
        String uid=seatUids.get(seat);
        if(uid==null){
            if(mySeat>0){toast("Leave your current video seat first");return;}
            takeSeat940(seat);return;
        }
        if(me!=null&&me.getUid().equals(uid)){leaveSeat940();return;}
        // Tapping occupied avatar opens actual KING account profile. Host
        // long-presses to remove; a normal tap must never kick somebody.
        Intent viewProfile9921=new Intent(this,KingPublicProfileActivity.class);
        viewProfile9921.putExtra("uid",uid);
        viewProfile9921.putExtra("name",safe(seatNames.get(seat),"KING member"));
        viewProfile9921.putExtra("roomId",roomId);
        startActivity(viewProfile9921);
    }
    private void toggleSeat940(){
        if(mySeat>0){leaveSeat940();return;}
        for(int n=1;n<=6;n++)if(!seatUids.containsKey(n)){takeSeat940(n);return;}
        toast("All 6 live video seats are occupied");
    }
    private void takeSeat940(int seat){
        if(!videoReady9921()||seat<1||seat>6){toast("Join live Party first");return;}
        if(videoActionBusy9921)return;
        final String uid=me.getUid(),name=displayName;
        final DocumentReference target=videoSeat9921(seat),registry=videoSeatOwner9921(uid);
        final DocumentReference membership=db.collection("live_rooms").document(roomId)
            .collection("members").document(uid);
        videoActionBusy9921=true;
        db.runTransaction(tx->{
            DocumentSnapshot member=tx.get(membership),already=tx.get(registry),taken=tx.get(target);
            if(!member.exists()||!uid.equals(member.getString("uid")))
                throw new IllegalStateException("Not in this live Party");
            if(already.exists()||taken.exists())throw new IllegalStateException("Seat already occupied");
            Map<String,Object> data=new HashMap<>();data.put("uid",uid);data.put("name",name);
            data.put("micOn",false);data.put("cameraOn",false);
            data.put("updatedAt",FieldValue.serverTimestamp());
            tx.set(target,data);
            Map<String,Object> owner=new HashMap<>();owner.put("uid",uid);
            owner.put("seatNo",seat);owner.put("updatedAt",FieldValue.serverTimestamp());
            tx.set(registry,owner);return null;
        }).addOnSuccessListener(v->{
            videoActionBusy9921=false;
            Map<String,Object> e=new HashMap<>();e.put("seatNo",seat);
            emit940("video_seat_claim",name+" joined video seat "+seat,e);
        }).addOnFailureListener(e->{videoActionBusy9921=false;toast("Seat not claimed: "+e.getMessage());});
    }
    private void releaseVideoSeat9921(int seat,Runnable done){
        if(db==null||me==null||seat<1){if(done!=null)done.run();return;}
        final String uid=me.getUid();
        final DocumentReference target=videoSeat9921(seat),registry=videoSeatOwner9921(uid);
        db.runTransaction(tx->{
            DocumentSnapshot taken=tx.get(target),owner=tx.get(registry);
            if(!taken.exists()||!uid.equals(taken.getString("uid")))return false;
            tx.delete(target);
            if(owner.exists()&&uid.equals(owner.getString("uid"))&&
                owner.getLong("seatNo")!=null&&owner.getLong("seatNo").intValue()==seat)
                tx.delete(registry);
            return true;
        }).addOnCompleteListener(task->{
            if(task.isSuccessful()&&Boolean.TRUE.equals(task.getResult())){
                Map<String,Object> data=new HashMap<>();data.put("seatNo",seat);
                emit940("video_seat_leave",displayName+" left video seat "+seat,data);
            }else if(!task.isSuccessful())
                KingStability.nonFatal(this,"video-seat-leave-9921",task.getException());
            if(done!=null)done.run();
        });
    }
    private void leaveSeat940(){if(mySeat>0&&!videoActionBusy9921)releaseVideoSeat9921(mySeat,null);}
    private void removeSeat940(int seat){
        if(!moderator||!videoReady9921()||videoActionBusy9921)return;
        final String uid=seatUids.get(seat);if(uid==null)return;
        final DocumentReference target=videoSeat9921(seat),registry=videoSeatOwner9921(uid);
        videoActionBusy9921=true;
        db.runTransaction(tx->{
            DocumentSnapshot old=tx.get(target),owner=tx.get(registry);
            if(!old.exists()||!uid.equals(old.getString("uid")))return false;
            tx.delete(target);
            if(owner.exists()&&uid.equals(owner.getString("uid"))&&owner.getLong("seatNo")!=null
                &&owner.getLong("seatNo").intValue()==seat)tx.delete(registry);
            return true;
        }).addOnSuccessListener(removed->{
            videoActionBusy9921=false;
            if(Boolean.TRUE.equals(removed)){
                Map<String,Object> e=new HashMap<>();e.put("seatNo",seat);e.put("targetUid",uid);
                emit940("video_remove",displayName+" removed video seat "+seat,e);
            }
        }).addOnFailureListener(err->{videoActionBusy9921=false;toast("Remove seat failed: "+err.getMessage());});
    }
    private void setVideoMedia9921(String field,boolean next){
        if(mySeat<1||!videoReady9921()){toast("Take a live video seat first");return;}
        final int seat=mySeat;final String uid=me.getUid();
        final DocumentReference ref=videoSeat9921(seat);
        db.runTransaction(tx->{
            DocumentSnapshot current=tx.get(ref);
            if(!current.exists()||!uid.equals(current.getString("uid")))
                throw new IllegalStateException("Video seat changed owner");
            tx.update(ref,field,next,"updatedAt",FieldValue.serverTimestamp());return null;
        }).addOnFailureListener(err->toast("Video control failed: "+err.getMessage()));
    }
    private void toggleMic940(){setVideoMedia9921("micOn",!micOn);}
    private void toggleCamera940(){setVideoMedia9921("cameraOn",!cameraOn);}

    private void inviteToVideoSeat940(int seat){
        if(!moderator||!videoReady9921()){toast("Host/co-host only");return;}
        db.collection("live_rooms").document(roomId).collection("members").limit(100)
          .get().addOnSuccessListener(snap->{
            if(isFinishing()||isDestroyed())return;
            List<DocumentSnapshot> docs=new ArrayList<>();List<String> rows=new ArrayList<>();
            for(DocumentSnapshot member:snap.getDocuments()){
                String uid=member.getString("uid");
                if(uid==null||uid.isEmpty()||uid.equals(me.getUid())||seatUids.containsValue(uid))continue;
                docs.add(member);rows.add(safe(member.getString("name"),"KING User"));
            }
            if(rows.isEmpty()){toast("No available members to invite");return;}
            new android.app.AlertDialog.Builder(this).setTitle("Invite to video seat "+seat)
              .setItems(rows.toArray(new String[0]),(dlg,w)->{
                final DocumentSnapshot selected=docs.get(w);
                final String uid=selected.getString("uid");
                Map<String,Object> invite=new HashMap<>();invite.put("recipientUid",uid);
                invite.put("seatNo",seat);invite.put("actorUid",me.getUid());
                invite.put("actorName",displayName);
                invite.put("createdAt",FieldValue.serverTimestamp());
                db.collection("live_rooms").document(roomId).collection("video_seat_invites")
                  .document(uid).set(invite)
                  .addOnSuccessListener(v->toast("Seat invitation sent"))
                  .addOnFailureListener(err->toast("Invite failed: "+err.getMessage()));
              }).setNegativeButton("Close",null).show();
          }).addOnFailureListener(e->toast("Members unavailable: "+e.getMessage()));
    }

    private void emit940(String type,String text,Map<String,Object> extras){
        if(db==null||me==null||roomId.isEmpty())return;Map<String,Object>d=new HashMap<>();d.put("actorUid",me.getUid());d.put("actorName",displayName);d.put("type",type);d.put("text",text);d.put("createdAt",FieldValue.serverTimestamp());if(extras!=null)d.putAll(extras);events940().add(d).addOnFailureListener(e->toast("Multi Video sync failed"));
    }

    private void applyMedia940(){applyAudio940(mySeat<1||!micOn);applyVideo940(mySeat<1||!cameraOn);}
    private void applyAudio940(boolean muted){try{Intent i=org.jitsi.meet.sdk.BroadcastIntentHelper.buildSetAudioMutedIntent(muted);LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);}catch(Throwable ignored){}}
    private void applyVideo940(boolean muted){try{Intent i=org.jitsi.meet.sdk.BroadcastIntentHelper.buildSetVideoMutedIntent(muted);LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);}catch(Throwable ignored){}}
    private DocumentReference matchRequest9922(String recipientUid){
        return db.collection("live_rooms").document(roomId).collection("video_match_requests").document(recipientUid);
    }
    private void watchMatchReply9922(String recipientUid){
        if(videoMatchOutgoing9922!=null){videoMatchOutgoing9922.remove();videoMatchOutgoing9922=null;}
        currentMatchTarget9922=recipientUid;
        videoMatchOutgoing9922=matchRequest9922(recipientUid).addSnapshotListener((doc,err)->{
            if(leaving940||isFinishing()||isDestroyed()||err!=null||doc==null||!doc.exists())return;
            if(!recipientUid.equals(currentMatchTarget9922)||me==null||
                !me.getUid().equals(doc.getString("fromUid")))return;
            String outcome=safe(doc.getString("status"),"");
            if(!"accepted".equals(outcome)&&!"declined".equals(outcome))return;
            if(videoMatchOutgoing9922!=null){videoMatchOutgoing9922.remove();videoMatchOutgoing9922=null;}
            currentMatchTarget9922="";
            if("accepted".equals(outcome)){
                new android.app.AlertDialog.Builder(this).setTitle("💞 Match confirmed")
                    .setMessage(safe(doc.getString("recipientName"),"KING Player")+" accepted your Multi Video match invitation.")
                    .setPositiveButton("OK",null).show();
            }else toast("Match invitation was declined");
        });
    }
    private void sendVideoMatchRequest9922(int targetSeat){
        if(!videoReady9921()||mySeat<1||targetSeat==mySeat)return;
        String recipient=seatUids.get(targetSeat);
        if(recipient==null||recipient.isEmpty()||recipient.equals(me.getUid()))return;
        final int sendingSeat=mySeat;final String sender=me.getUid();
        final String recipientName=safe(seatNames.get(targetSeat),"KING Player");
        final DocumentReference mine=videoSeat9921(sendingSeat),theirs=videoSeat9921(targetSeat);
        final DocumentReference request=matchRequest9922(recipient);
        if(videoActionBusy9921)return;videoActionBusy9921=true;
        db.runTransaction(tx->{
            DocumentSnapshot from=tx.get(mine),to=tx.get(theirs);
            if(!from.exists()||!sender.equals(from.getString("uid"))||
               !to.exists()||!recipient.equals(to.getString("uid")))
                throw new IllegalStateException("Either video seat is no longer occupied");
            // Firestore security rules enforce the pending-request cooldown.
            Map<String,Object> payload=new HashMap<>();
            payload.put("fromUid",sender);payload.put("fromName",displayName);
            payload.put("recipientUid",recipient);payload.put("recipientName",recipientName);
            payload.put("fromSeat",sendingSeat);payload.put("toSeat",targetSeat);
            payload.put("status","pending");payload.put("createdAt",FieldValue.serverTimestamp());
            payload.put("updatedAt",FieldValue.serverTimestamp());
            tx.set(request,payload);return null;
        }).addOnSuccessListener(v->{
            videoActionBusy9921=false;
            if(leaving940||isFinishing()||isDestroyed())return;
            toast("Match request sent • waiting for "+recipientName);
            watchMatchReply9922(recipient);
        }).addOnFailureListener(err->{videoActionBusy9921=false;
            if(!leaving940)toast("Match request failed: "+err.getMessage());});
    }
    private void showVideoMatchRequest9922(DocumentSnapshot request){
        if(mySeat<1||me==null)return;
        final String sender=safe(request.getString("fromUid"),"");
        final Long fromNo=request.getLong("fromSeat"),toNo=request.getLong("toSeat");
        if(sender.isEmpty()||fromNo==null||toNo==null||
            !me.getUid().equals(request.getString("recipientUid")))return;
        final int fromSeat=fromNo.intValue(),targetSeat=toNo.intValue();
        new android.app.AlertDialog.Builder(this).setTitle("💞 Video match request")
            .setMessage(safe(request.getString("fromName"),"KING member")+
                " invited you to match video seats "+fromSeat+" and "+targetSeat+". Accept?")
            .setPositiveButton("Accept",(dialog,n)->respondVideoMatch9922(request,true,fromSeat,targetSeat,sender))
            .setNegativeButton("Decline",(dialog,n)->respondVideoMatch9922(request,false,fromSeat,targetSeat,sender))
            .show();
    }
    private void respondVideoMatch9922(DocumentSnapshot request,boolean accept,int fromSeat,int targetSeat,String sender){
        if(!videoReady9921()||me==null||mySeat!=targetSeat){toast("Join the invited video seat first");return;}
        final String recipient=me.getUid();final DocumentReference ref=request.getReference();
        final DocumentReference from=videoSeat9921(fromSeat),to=videoSeat9921(targetSeat);
        db.runTransaction(tx->{
            DocumentSnapshot r=tx.get(ref),a=tx.get(from),b=tx.get(to);
            if(!r.exists()||!"pending".equals(r.getString("status"))||
                !recipient.equals(r.getString("recipientUid"))||!sender.equals(r.getString("fromUid")))
                throw new IllegalStateException("Match request expired");
            Timestamp sent=r.getTimestamp("createdAt");
            if(sent==null||System.currentTimeMillis()-sent.toDate().getTime()>120000L)
                throw new IllegalStateException("Match request too old");
            if(!a.exists()||!sender.equals(a.getString("uid"))||
                !b.exists()||!recipient.equals(b.getString("uid")))
                throw new IllegalStateException("Either video seat has changed");
            tx.update(ref,"status",accept?"accepted":"declined","updatedAt",FieldValue.serverTimestamp());
            return null;
        }).addOnSuccessListener(v->{if(!leaving940)toast(accept?"Match accepted":"Match declined");})
          .addOnFailureListener(err->{if(!leaving940)toast("Match response failed: "+err.getMessage());});
    }
    private void videoQuickMatch940(){
        if(!videoReady9921()||mySeat<1){toast("Sit in an authenticated live Video seat first");return;}
        List<Integer> seats=new ArrayList<>();List<String> labels=new ArrayList<>();
        for(int i=1;i<=6;i++){String uid=seatUids.get(i);
            if(uid==null||uid.isEmpty()||i==mySeat)continue;
            seats.add(i);labels.add("Seat "+i+" • "+safe(seatNames.get(i),"KING member"));}
        if(seats.isEmpty()){toast("No other seated player available");return;}
        new android.app.AlertDialog.Builder(this).setTitle("💞 Invite to Live Video Match")
            .setItems(labels.toArray(new String[0]),(d,index)->sendVideoMatchRequest9922(seats.get(index)))
            .setNegativeButton("Cancel",null).show();
    }

    private void refreshControls940(){if(stateText!=null)stateText.setText(mySeat>0?("📹 Video seat "+mySeat+(moderator?" • Host controls":"")):("📹 Multi Video • viewer mode"+(moderator?" • Host controls":"")));if(seatAction!=null)seatAction.setText(mySeat>0?"↥ Leave Seat":"＋ Sit Down");if(micAction!=null){micAction.setText(micOn?"🎤 Mic ON":"🎤 Mic OFF");micAction.setAlpha(mySeat>0?1f:.45f);}if(camAction!=null){camAction.setText(cameraOn?"📷 Camera ON":"📷 Camera OFF");camAction.setAlpha(mySeat>0?1f:.45f);}if(matchAction!=null)matchAction.setAlpha(mySeat>0?1f:.45f);}

    private void leaveAndFinish940(){
        if(leaving940)return;leaving940=true;
        heartbeat940.removeCallbacks(heartbeatTask940);
        if(mySeat>0)releaseVideoSeat9921(mySeat,this::finish);
        else finish();
    }
    @android.annotation.SuppressLint("MissingSuperCall")
    @Override public void onBackPressed(){leaveAndFinish940();}
    @Override protected void onResume(){super.onResume();try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostResume(this);}catch(Throwable e){KingStability.nonFatal(this,"video-jitsi-resume",e);}}
    @Override protected void onStop(){try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostPause(this);}catch(Throwable e){KingStability.nonFatal(this,"video-jitsi-pause",e);}super.onStop();}
    @Override public void onNewIntent(Intent intent){super.onNewIntent(intent);try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onNewIntent(intent);}catch(Throwable e){KingStability.nonFatal(this,"video-jitsi-intent",e);}}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onActivityResult(this,requestCode,resultCode,data);}catch(Throwable e){KingStability.nonFatal(this,"video-jitsi-result",e);}}
    @Override public void requestPermissions(String[] permissions,int requestCode,com.facebook.react.modules.core.PermissionListener listener){try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.requestPermissions(this,permissions,requestCode,listener);}catch(Throwable e){KingStability.nonFatal(this,"video-jitsi-permission-request",e);}}
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){super.onRequestPermissionsResult(requestCode,permissions,grantResults);try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode,permissions,grantResults);}catch(Throwable e){KingStability.nonFatal(this,"video-jitsi-permission-result",e);}}
    @Override protected void onDestroy(){heartbeat940.removeCallbacks(heartbeatTask940);if(eventsListener!=null)eventsListener.remove();if(roomListener!=null)roomListener.remove();if(roleListener!=null)roleListener.remove();
        if(videoSeatListener9921!=null)videoSeatListener9921.remove();
        if(videoInviteListener9921!=null)videoInviteListener9921.remove();
        if(selfMemberListener9921!=null)selfMemberListener9921.remove();
        if(videoMatchIncoming9922!=null)videoMatchIncoming9922.remove();
        if(videoMatchOutgoing9922!=null)videoMatchOutgoing9922.remove();
        try{if(meetView!=null){meetView.dispose();meetView=null;}}catch(Throwable ignored){}try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostDestroy(this);}catch(Throwable e){KingStability.nonFatal(this,"video-jitsi-destroy",e);}super.onDestroy();}
}