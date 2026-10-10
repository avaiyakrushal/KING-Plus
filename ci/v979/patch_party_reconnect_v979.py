#!/usr/bin/env python3
"""KING Plus v9.7.9 — repair Firestore offline Party join on two real phones.

Rebuild directly on the successful v9.7.8 source artifact. New behavior:
* A RETRY actually re-enables Firestore network, refreshes the Firebase token,
  confirms room SERVER existence, then rejoins under SAME room/user/session.
* One automatic repair on transport UNAVAILABLE / "client is offline" only.
* Connectivity recovered while in a Party triggers server re-verification.
* No offline cache / listener success can bypass Firestore security/ban checks.
* All async retries are generation-guarded and timeout safely to error UI.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
file=pkg/'PartyActivity.java'
s=file.read_text()
shutil.copy2(Path(__file__).with_name('KingPartyConnection979.java'),pkg/'KingPartyConnection979.java')

def once(old,new,label):
    global s
    n=s.count(old)
    if n!=1:raise SystemExit(f'{label}: expected unique marker, got {n}: {old[:130]!r}')
    s=s.replace(old,new,1)
    print('PASS',label)

insert='    private void joinMemberThenOpen891(){'
extra=r'''    private int partyReconnectGeneration979;
    private boolean partyReconnecting979;
    private boolean partyAutoRepairUsed979;
    private Runnable partyReconnectTimeout979;

    private void clearPartyReconnect979(){
        if(partyReconnectTimeout979!=null){
            partyJoinHandler977.removeCallbacks(partyReconnectTimeout979);
            partyReconnectTimeout979=null;
        }
        partyReconnecting979=false;
    }

    private boolean currentReconnect979(String expectedRoom979,String expectedUid979,int generation979){
        com.google.firebase.auth.FirebaseUser actual979=null;
        try{actual979=com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();}
        catch(Exception ignored){}
        return KingPartyConnection979.canRejoin(expectedRoom979,roomId,
            expectedUid979,actual979==null?null:actual979.getUid(),
            generation979,partyReconnectGeneration979,
            !isFinishing()&&!isDestroyed(),cloudRoom&&!memberSeen);
    }

    private void reconnectPartyFirestore979(boolean refreshToken979){
        if(isFinishing()||isDestroyed()||partyReconnecting979)return;
        if(db==null||!cloudRoom||roomId==null||roomId.trim().isEmpty()){
            showPartyJoinFailure977("Choose an active Party Room and sign in first.");return;
        }
        if(!KingNetwork.online(this)){
            showPartyJoinFailure977("Android reports no validated Internet connection. "+
                "Try mobile data instead of Wi-Fi, disable VPN/Private DNS, then tap Retry.");
            return;
        }
        try{user=com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();}
        catch(Exception ignored){user=null;}
        if(user==null){
            showPartyJoinFailure977("Firebase sign-in session is missing. "+
                "Go back and sign in using your Google/verified mobile account.");return;
        }
        final String expectedRoom979=roomId;
        final String expectedUid979=user.getUid();
        final int generation979=++partyReconnectGeneration979;
        partyReconnecting979=true;
        ++roomJoinGeneration966;  // Invalidate pending writes from previous offline attempt.
        stopPartyJoinTimeout977();
        showJoiningRoom964();
        KingCrashWatch958.mark(this,"party-firestore-network-recovery-979");
        partyReconnectTimeout979=()->{
            if(!currentReconnect979(expectedRoom979,expectedUid979,generation979))return;
            showPartyJoinFailure977("Firestore reconnect took more than 18 seconds. "+
                "Try switching Wi-Fi / mobile data or disabling VPN. "+
                "The Party Room was NOT joined without verification.");
        };
        partyJoinHandler977.postDelayed(partyReconnectTimeout979,18000L);
        db.enableNetwork()
            .addOnSuccessListener(v->{
                if(!currentReconnect979(expectedRoom979,expectedUid979,generation979))return;
                user.getIdToken(refreshToken979)
                    .addOnSuccessListener(token979->{
                        if(!currentReconnect979(expectedRoom979,expectedUid979,generation979))return;
                        db.collection("live_rooms").document(expectedRoom979)
                            .get(com.google.firebase.firestore.Source.SERVER)
                            .addOnSuccessListener(room979->{
                                if(!currentReconnect979(expectedRoom979,expectedUid979,generation979))return;
                                clearPartyReconnect979();
                                if(room979==null||!room979.exists()||
                                   Boolean.TRUE.equals(room979.getBoolean("closed"))){
                                    showPartyJoinFailure977("The exact Party Room was removed or closed. "+
                                        "Ask the host to share a new full Room invitation.");
                                    return;
                                }
                                if(room979.getString("ownerUid")==null){
                                    showPartyJoinFailure977("Room record has no valid Host UID. "+
                                        "Ask the Host to recreate the Room.");return;
                                }
                                // Never change the Room ID, signed-in account, room access
                                // or private/password checks while recovering transport.
                                joinMemberThenOpen891();
                            })
                            .addOnFailureListener(err979->{
                                if(currentReconnect979(expectedRoom979,expectedUid979,generation979))
                                    showPartyJoinFailure977("Firebase room read still failed after reconnect: "+
                                        msg(err979)+". Try mobile data or check Firebase access.");
                            });
                    })
                    .addOnFailureListener(err979->{
                        if(currentReconnect979(expectedRoom979,expectedUid979,generation979))
                            showPartyJoinFailure977("Your Firebase login token could not refresh: "+
                                msg(err979)+". Sign in again and retry.");
                    });
            })
            .addOnFailureListener(err979->{
                if(currentReconnect979(expectedRoom979,expectedUid979,generation979))
                    showPartyJoinFailure977("Could not enable Firebase network: "+msg(err979)+
                        ". Verify internet/VPN and try again.");
            });
    }

    private boolean firestoreTransportError979(Exception error979){
        String code979="";
        if(error979 instanceof com.google.firebase.firestore.FirebaseFirestoreException)
            code979=((com.google.firebase.firestore.FirebaseFirestoreException)error979)
                .getCode().name();
        return KingPartyConnection979.isRetryable(code979,error979==null?null:error979.getMessage());
    }

'''
once(insert,extra+insert,'install session-safe Firestore repair with authenticated server verification')

once(
'''                KingStability.nonFatal(this,"party-member-lookup-966",error);
                roomJoinError891("Cannot verify signed-in Party membership",error);''',
'''                KingStability.nonFatal(this,"party-member-lookup-966",error);
                if(firestoreTransportError979(error)&&!partyAutoRepairUsed979&&
                   KingNetwork.online(this)){
                    partyAutoRepairUsed979=true;
                    reconnectPartyFirestore979(false);
                    return;
                }
                roomJoinError891("Cannot verify signed-in Party membership",error);''',
'try one automatic Firestore reconnect on genuine offline member lookup, not permission denied')

once(
'''        ++roomJoinGeneration966; // Old Firebase read/write callbacks cannot reopen an abandoned room.
        KingCrashWatch958.mark(this,"party-member-join-error-977");''',
'''        ++roomJoinGeneration966; // Old Firebase read/write callbacks cannot reopen an abandoned room.
        ++partyReconnectGeneration979;
        clearPartyReconnect979();
        KingCrashWatch958.mark(this,"party-member-join-error-977");''',
'cancel offline reconnect callbacks on error to avoid a late accidental room join')

once(
'''        TextView detail977=tv(reason977==null?"Room verification unavailable":reason977,
            14,0xffd5e0e7,false);
        detail977.setGravity(Gravity.CENTER);
        root977.addView(detail977,new LinearLayout.LayoutParams(-1,dp(140)));''',
'''        String actualUid979=user==null?"not signed in":user.getUid();
        String shownUid979=actualUid979.length()>8?actualUid979.substring(0,8)+"…":actualUid979;
        String hint979=KingPartyConnection979.status(KingNetwork.online(this),
            user!=null,true);
        String reasonUi979=(reason977==null?"Room verification unavailable":reason977);
        TextView detail977=tv(reasonUi979+"\\n\\n"+hint979+
            "\\nRoom: "+(roomId==null?"none":shortId(roomId))+
            " • UID: "+shownUid979+
            "\\nBoth phones: same KING Plus APK, two DIFFERENT Firebase accounts.",
            12,0xffd5e0e7,false);
        detail977.setGravity(Gravity.CENTER);
        root977.addView(detail977,new LinearLayout.LayoutParams(-1,dp(250)));''',
'clearly diagnose transport/auth and show room/UID before two-phone retry')

once(
'''            showJoiningRoom964();
            joinMemberThenOpen891();
        });
        TextView back977''',
'''            reconnectPartyFirestore979(true);
        });
        TextView back977''',
'Retry now re-enables Firestore, refreshes auth and checks same server room before joining')

once(
'''        ++presenceGeneration967;
        presenceSession967=java.util.UUID.randomUUID().toString();
        roomId=id.trim();''',
'''        ++presenceGeneration967;
        partyAutoRepairUsed979=false;
        ++partyReconnectGeneration979;
        clearPartyReconnect979();
        presenceSession967=java.util.UUID.randomUUID().toString();
        roomId=id.trim();''',
'reset only auto-recovery quota on each fresh full Room ID')

once(
'''        stopPartyJoinTimeout977();
        currentLobbyTab940=''',
'''        stopPartyJoinTimeout977();
        ++partyReconnectGeneration979;clearPartyReconnect979();
        currentLobbyTab940=''',
'cancel pending Firestore network reconnect when returning to lobby')

once(
'''        partyNetworkCallback940=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!partyLastOnline940;partyLastOnline940=online;
            if(recovered&&roomId==null&&!isFinishing()&&!isDestroyed())renderLobby(currentLobbyTab940);
        }));''',
'''        partyNetworkCallback940=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!partyLastOnline940;partyLastOnline940=online;
            if(!recovered||isFinishing()||isDestroyed())return;
            if(roomId==null){renderLobby(currentLobbyTab940);return;}
            if(cloudRoom&&db!=null){
                db.enableNetwork().addOnSuccessListener(v->{
                    if(isFinishing()||isDestroyed())return;
                    if(cloudRoom&&roomId!=null&&memberSeen){
                        ensureRoomMembership900();
                    }else if(cloudRoom&&roomId!=null&&!memberSeen&&
                        !partyReconnecting979&&!partyAutoRepairUsed979){
                        partyAutoRepairUsed979=true;
                        reconnectPartyFirestore979(false);
                    }
                });
            }
        }));''',
'recover a pending room join or refresh member presence when validated internet returns')

once(
'''        stopPartyJoinTimeout977();memberSeen=true;renderParty();startHeartbeat900();''',
'''        stopPartyJoinTimeout977();clearPartyReconnect979();memberSeen=true;renderParty();startHeartbeat900();''',
'clear reconnect state only after Firebase positively confirms member write')

once(
'''    @Override protected void onDestroy(){stopPartyJoinTimeout977();++roomJoinGeneration966;''',
'''    @Override protected void onDestroy(){stopPartyJoinTimeout977();++partyReconnectGeneration979;clearPartyReconnect979();++roomJoinGeneration966;''',
'close all reconnect callbacks on destroyed Activity')

file.write_text(s)
gradle=root/'app/build.gradle'
g=gradle.read_text()
old="versionCode 169; versionName '9.7.8-startup-signal-attribution'"
if g.count(old)!=1:raise SystemExit('Expected successful v9.7.8 Android source baseline')
gradle.write_text(g.replace(old,"versionCode 170; versionName '9.7.9-party-firestore-reconnect'",1))
print('PASS v9.7.9 Party reconnect patch installed')
