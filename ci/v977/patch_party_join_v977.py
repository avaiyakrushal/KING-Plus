#!/usr/bin/env python3
"""Fix indefinitely frozen Party 'Verifying your account and room access' in v9.7.6.

A late/failing Firestore member/room/seat query could leave the Activity with
only the loading view forever. Add a session-safe watchdog, Retry & Back buttons,
server-only permission reads, and an actionable error panel. All pending
callbacks are invalidated on timeout/retry/lobby transitions. No bypass of
Firebase membership or access checks; no new billing/APIs.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
file=pkg/'PartyActivity.java'
s=file.read_text()
shutil.copy2(Path(__file__).with_name('KingPartyJoinTimeout977.java'),
             pkg/'KingPartyJoinTimeout977.java')

def once(old,new,label):
 global s
 count=s.count(old)
 if count!=1:raise SystemExit(f'{label}: expected one marker, got {count}: {old[:180]!r}')
 s=s.replace(old,new,1)
 print('PASS',label)

anchor='    private void joinMemberThenOpen891(){'
extra=r'''    // A Firebase .get()/set() task can wait indefinitely on a slow/offline device.
    // Never leave a fullscreen spinner without Retry or an exit route.
    private final android.os.Handler partyJoinHandler977=
        new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable partyJoinTimeoutTask977;

    private void stopPartyJoinTimeout977(){
        if(partyJoinTimeoutTask977!=null){
            partyJoinHandler977.removeCallbacks(partyJoinTimeoutTask977);
            partyJoinTimeoutTask977=null;
        }
    }

    private void startPartyJoinTimeout977(String expectedRoom977,String expectedUid977,int token977){
        stopPartyJoinTimeout977();
        final long begin977=android.os.SystemClock.elapsedRealtime();
        partyJoinTimeoutTask977=()->{
            final String currentUid977=user==null?null:user.getUid();
            if(!KingPartyJoinTimeout977.waiting(expectedRoom977,roomId,
                    expectedUid977,currentUid977,token977,roomJoinGeneration966,
                    cloudRoom&&!isFinishing()&&!isDestroyed(),memberSeen))return;
            if(!KingPartyJoinTimeout977.expired(begin977,
                    android.os.SystemClock.elapsedRealtime()))return;
            KingCrashWatch958.mark(this,"party-member-join-timeout-977");
            showPartyJoinFailure977(
                "Firebase did not confirm room membership within 18 seconds.\n\n"+
                "Check internet, Google/Phone sign-in, and Firebase access rules. "+
                "You may retry or return to the Party list.");
        };
        partyJoinHandler977.postDelayed(partyJoinTimeoutTask977,
            KingPartyJoinTimeout977.JOIN_TIMEOUT_MS);
    }

    private void showPartyJoinFailure977(String reason977){
        if(isFinishing()||isDestroyed())return;
        stopPartyJoinTimeout977();
        ++roomJoinGeneration966; // Old Firebase read/write callbacks cannot reopen an abandoned room.
        KingCrashWatch958.mark(this,"party-member-join-error-977");
        LinearLayout root977=new LinearLayout(this);
        root977.setOrientation(LinearLayout.VERTICAL);
        root977.setGravity(Gravity.CENTER);
        root977.setPadding(dp(22),dp(22),dp(22),dp(22));
        root977.setBackgroundColor(0xff172b36);
        TextView title977=tv("⚠  Party Room connection failed",20,Color.WHITE,true);
        title977.setGravity(Gravity.CENTER);
        root977.addView(title977,new LinearLayout.LayoutParams(-1,dp(90)));
        TextView detail977=tv(reason977==null?"Room verification unavailable":reason977,
            14,0xffd5e0e7,false);
        detail977.setGravity(Gravity.CENTER);
        root977.addView(detail977,new LinearLayout.LayoutParams(-1,dp(140)));
        TextView retry977=tv("↻  Retry secure connection",16,Color.WHITE,true);
        retry977.setGravity(Gravity.CENTER);
        retry977.setBackgroundColor(0xff12856d);
        LinearLayout.LayoutParams retryParams977=
            new LinearLayout.LayoutParams(-1,dp(52));
        retryParams977.setMargins(0,dp(14),0,0);
        root977.addView(retry977,retryParams977);
        retry977.setOnClickListener(v->{
            if(isFinishing()||isDestroyed())return;
            if(!KingNetwork.online(this)){
                toast("Internet is disconnected. Reconnect and retry.");return;
            }
            try{user=FirebaseAuth.getInstance().getCurrentUser();}
            catch(Exception ignored){}
            if(user==null||db==null||roomId==null||roomId.trim().isEmpty()){
                toast("Sign in and choose the Party again");
                renderLobby("Hot");return;
            }
            showJoiningRoom964();
            joinMemberThenOpen891();
        });
        TextView back977=tv("←  Back to Party list",16,Color.WHITE,true);
        back977.setGravity(Gravity.CENTER);
        back977.setBackgroundColor(0xff324658);
        LinearLayout.LayoutParams backParams977=
            new LinearLayout.LayoutParams(-1,dp(52));
        backParams977.setMargins(0,dp(14),0,0);
        root977.addView(back977,backParams977);
        back977.setOnClickListener(v->renderLobby("Hot"));
        setSafeContentView(root977);
    }

'''
once(anchor,extra+anchor,'install guarded 18s Firebase join watchdog and error/retry/back page')

once(
 '''    private void joinMemberThenOpen891(){
        if(user==null||db==null||roomId==null||roomId.trim().isEmpty()||!cloudRoom){
            toast("Sign in and choose an active Party Room to join");
            return;
        }''',
 '''    private void joinMemberThenOpen891(){
        if(user==null||db==null||roomId==null||roomId.trim().isEmpty()||!cloudRoom){
            showPartyJoinFailure977("Sign in and choose an active Party Room to join.");
            return;
        }''',
 'invalid account and room state display exit/retry rather than infinite loading')
once(
 '''        final int requestToken966=++roomJoinGeneration966;
        final DocumentReference requestedRoomRef966''',
 '''        final int requestToken966=++roomJoinGeneration966;
        startPartyJoinTimeout977(requestedRoom966,requestedUid966,requestToken966);
        final DocumentReference requestedRoomRef966''',
 'schedule watchdog for each authorized Firebase join request')

# Do not claim a room is accessible based on stale Firestore disk cache.
# get(Source.SERVER) gives an error when unreachable, which becomes actionable UI.
start=s.index('    private void joinMemberThenOpen891(){')
end=s.index('    private void checkCrowdCapacity930(',start)
segment=s[start:end]
if segment.count('        ref.get()')!=1:
    raise SystemExit('Member read source marker missing')
segment=segment.replace('        ref.get()',
        '        ref.get(com.google.firebase.firestore.Source.SERVER)',1)
old='''                KingStability.nonFatal(this,"party-member-lookup-966",error);
                checkCrowdCapacity930(requestedRoomRef966,requestedRoom966,requestedUid966,requestToken966,write);'''
if segment.count(old)!=1:raise SystemExit('Member failure fallback changed')
segment=segment.replace(old,
 '''                KingStability.nonFatal(this,"party-member-lookup-966",error);
                roomJoinError891("Cannot verify signed-in Party membership",error);''')
s=s[:start]+segment+s[end:]
print('PASS', 'Require server-verified member identity, fail visibly instead of retrying bypass after read error')

start=s.index('    private void checkCrowdCapacity930(')
end=s.index('    private Map<String,Object> minimalMember921()',start)
segment=s[start:end]
if segment.count('expectedRoom966.get()')!=1:raise SystemExit('Missing server room get')
segment=segment.replace('expectedRoom966.get()',
    'expectedRoom966.get(com.google.firebase.firestore.Source.SERVER)',1)
if segment.count('expectedRoom966.collection("members").get()')!=1:
    raise SystemExit('Missing member capacity check')
segment=segment.replace('expectedRoom966.collection("members").get()',
    'expectedRoom966.collection("members").get(com.google.firebase.firestore.Source.SERVER)',1)
segment=segment.replace(
 '''        if(expectedRoom966==null)return;''',
 '''        if(expectedRoom966==null){
            showPartyJoinFailure977("Invalid Party Room reference");return;
        }''',1)
old='if(members==null){toast("Unable to verify Party members");return;}'
if segment.count(old)!=1:raise SystemExit('Missing unexpected member snapshot failure')
segment=segment.replace(old,
 'if(members==null){showPartyJoinFailure977("Cannot verify Party Room members");return;}',1)
s=s[:start]+segment+s[end:]
print('PASS', 'Server-confirm capacity and show error for null result instead of orphaned spinner')

once(
 '        memberSeen=true;renderParty();startHeartbeat900();',
 '        stopPartyJoinTimeout977();memberSeen=true;renderParty();startHeartbeat900();',
 'cancel watchdog immediately before real verified member appears')

once(
 '    private void renderLobby(String selected) {\n',
 '    private void renderLobby(String selected) {\n        stopPartyJoinTimeout977();\n',
 'cancel watchdog and invalidate pending retries when returning to lobby')

# Existing roomJoinError891 displayed OK dialog over the endless spinner,
# with no route back to Party list. Only intercept errors while joining.
pos=s.index('    private void roomJoinError891(')
b=s.index('{',pos)
s=s[:b+1]+r'''
        if(cloudRoom&&roomId!=null&&!memberSeen){
            showPartyJoinFailure977(title+"\n"+msg(e));
            return;
        }
'''+s[b+1:]
print('PASS', 'actual Firebase permission/room errors show Retry/Back instead of orphaned dialog')

# Ensure users can exit the loading screen immediately, not only after timeout.
start=s.index('    private void showJoiningRoom964(){')
end=s.index('    private void renderParty() {',start)
s=s[:start]+r'''    private void showJoiningRoom964(){
        KingCrashWatch958.mark(this,"party-member-sync-964");
        LinearLayout waiting964=new LinearLayout(this);
        waiting964.setOrientation(LinearLayout.VERTICAL);
        waiting964.setPadding(dp(20),dp(20),dp(20),dp(20));
        waiting964.setGravity(Gravity.CENTER);
        waiting964.setBackgroundColor(0xff172b36);
        TextView label964=tv("Connecting to Party…\nVerifying your account and room access",
            17,Color.WHITE,true);
        label964.setGravity(Gravity.CENTER);
        waiting964.addView(label964,new LinearLayout.LayoutParams(-1,dp(110)));
        android.widget.ProgressBar progress977=new android.widget.ProgressBar(this);
        LinearLayout.LayoutParams progressParams977=new LinearLayout.LayoutParams(dp(40),dp(40));
        progressParams977.gravity=Gravity.CENTER_HORIZONTAL;
        waiting964.addView(progress977,progressParams977);
        TextView hint977=tv("If verification takes too long, you can return to the Party list.",
            12,0xffbdd3dd,false);
        hint977.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintParams977=new LinearLayout.LayoutParams(-1,dp(75));
        waiting964.addView(hint977,hintParams977);
        TextView back977=tv("←  Cancel and return to Party",15,Color.WHITE,true);
        back977.setGravity(Gravity.CENTER);
        back977.setBackgroundColor(0xff314659);
        LinearLayout.LayoutParams backParams977=new LinearLayout.LayoutParams(-1,dp(52));
        backParams977.setMargins(0,dp(16),0,0);
        waiting964.addView(back977,backParams977);
        back977.setOnClickListener(v->renderLobby("Hot"));
        setSafeContentView(waiting964);
    }

'''+s[end:]
print('PASS', 'loading screen shows progress and immediate Cancel/Back button')

once(
 '    @Override protected void onDestroy(){++roomJoinGeneration966;',
 '    @Override protected void onDestroy(){stopPartyJoinTimeout977();++roomJoinGeneration966;',
 'remove delayed timeout callback when Activity is destroyed')

file.write_text(s)
g=root/'app/build.gradle'
text=g.read_text()
old="versionCode 167; versionName '9.7.6-unified-verified-wallet'"
if text.count(old)!=1:raise SystemExit('Expected compiled v9.7.6 baseline; versionCode differs')
g.write_text(text.replace(old,
 "versionCode 168; versionName '9.7.7-party-join-timeout-retry'",1))
print('PASS KING Plus v9.7.7 Party join watchdog, explicit errors and cancellation safely patched')
