#!/usr/bin/env python3
"""KING Plus v9.8.5 — Party connecting screen diagnostics and bounded Firebase test.

Patch verified v9.8.4 source artifact, NOT old repository root /app.
- Provide a direct on-phone connection test during the Party joining spinner.
- Leave/cancel the pending join BEFORE launching read-only diagnostics.
- Bound the existing Firebase test to 22s and discard stale asynchronous callbacks.
- Preserve Room security, no bypass, no billing or database writes.
"""
from pathlib import Path
import sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
party=pkg/'PartyActivity.java'
diagnostic=pkg/'KingConnection983Activity.java'
gradle=root/'app/build.gradle'

def once(source, old, new, label):
    found=source.count(old)
    if found!=1:
        raise SystemExit(f"{label}: expected exactly one marker, saw {found}: {old[:110]!r}")
    print("PASS",label)
    return source.replace(old,new,1)

s=party.read_text()
s=once(s,
'''        TextView back977=tv("←  Cancel and return to Party",15,Color.WHITE,true);''',
'''        TextView diagnose985=tv("🔌  Test Firebase / Room connection",15,Color.WHITE,true);
        diagnose985.setGravity(Gravity.CENTER);
        diagnose985.setBackgroundColor(0xff146e72);
        LinearLayout.LayoutParams diagnoseParams985=
            new LinearLayout.LayoutParams(-1,dp(52));
        diagnoseParams985.setMargins(0,dp(14),0,0);
        waiting964.addView(diagnose985,diagnoseParams985);
        diagnose985.setOnClickListener(v->{
            if(isFinishing()||isDestroyed())return;
            final String inspectingRoom985=roomId;
            // Abandon the join before opening a new Activity: never accept a
            // late Firebase membership/seat callback from the old screen.
            ++roomJoinGeneration966;
            ++partyReconnectGeneration979;
            clearPartyReconnect979();
            stopPartyJoinTimeout977();
            renderLobby("Hot");
            Intent connection985=new Intent(this,KingConnection983Activity.class);
            if(inspectingRoom985!=null&&!inspectingRoom985.trim().isEmpty())
                connection985.putExtra("roomInput983",inspectingRoom985);
            startActivity(connection985);
        });
        TextView back977=tv("←  Cancel and return to Party",15,Color.WHITE,true);''',
'Connecting page: direct safe Firebase test, cancels pending room join')
party.write_text(s)

s=diagnostic.read_text()
s=once(s,
'''    private boolean destroyed983;''',
'''    private boolean destroyed983;
    private final android.os.Handler diagnosticHandler985=
        new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable diagnosticTimer985;
    private static final long DIAGNOSTIC_TIMEOUT_985_MS=22000L;

    private void stopDiagnosticTimeout985(){
        if(diagnosticTimer985!=null){
            diagnosticHandler985.removeCallbacks(diagnosticTimer985);
            diagnosticTimer985=null;
        }
    }
    private void startDiagnosticTimeout985(int gen985,String uid985){
        stopDiagnosticTimeout985();
        diagnosticTimer985=()->{
            if(!current983(gen985,uid985))return;
            ++generation983; // prevents a late Firebase callback claiming success
            stopMemberWatch984();
            watchButton984.setEnabled(false);
            inspectedRoom984=null;
            inspectedUid984=null;
            inspectedGeneration984=0;
            stopDiagnosticTimeout985();
            addLine("✗ Firebase did not confirm the server test within 22 seconds.");
            addLine("Check mobile data / Wi-Fi, VPN, the account, and Firestore permissions.");
            addLine("Retry the test. The diagnostic never joins or changes a Room.");
        };
        diagnosticHandler985.postDelayed(diagnosticTimer985,DIAGNOSTIC_TIMEOUT_985_MS);
    }''',
'Bound read-only Firebase diagnostic to 22 seconds and cancel stale callbacks')

s=once(s,
'''    private void fail(int gen,String uid,Exception error,String operation){
        if(!current983(gen,uid))return;''',
'''    private void fail(int gen,String uid,Exception error,String operation){
        if(!current983(gen,uid))return;
        stopDiagnosticTimeout985();''',
'Stop timeout on a real Firestore error')

s=once(s,
'''    private void runTest983(){
        generation983++;''',
'''    private void runTest983(){
        stopDiagnosticTimeout985();
        generation983++;''',
'Retry clears old diagnostic timeout')

s=once(s,
'''        addLine("1. Refreshing signed-in Firebase token…");
        final FirebaseUser auth=FirebaseAuth.getInstance().getCurrentUser();
        if(auth==null)return;''',
'''        addLine("1. Refreshing signed-in Firebase token…");
        final FirebaseUser auth=FirebaseAuth.getInstance().getCurrentUser();
        if(auth==null){
            addLine("✗ Firebase sign-in changed during test. Sign in again.");
            return;
        }
        startDiagnosticTimeout985(gen,uid);''',
'Start bounded test only after validating account and Room input')

s=once(s,
'''                if(token==null||token.getToken()==null){
                    addLine("✗ Firebase token response was missing.");''',
'''                if(token==null||token.getToken()==null){
                    stopDiagnosticTimeout985();
                    addLine("✗ Firebase token response was missing.");''',
'Missing token is a terminal error')

s=once(s,
'''            .addOnFailureListener(error->{
                if(!current983(gen,uid))return;
                addLine("✗ Cannot refresh Firebase sign-in: "+''',
'''            .addOnFailureListener(error->{
                if(!current983(gen,uid))return;
                stopDiagnosticTimeout985();
                addLine("✗ Cannot refresh Firebase sign-in: "+''',
'Auth failure stops spinner timeout')

s=once(s,
'''                    addLine("✓ Firestore server reachable. Own public profile: "+''',
'''                    stopDiagnosticTimeout985();
                    addLine("✓ Firestore server reachable. Own public profile: "+''',
'Profile-only test completion cancels timeout')

s=once(s,
'''                    if(snapshot.isEmpty()){
                        addLine("✗ No Party Room has this code on Firebase server.");''',
'''                    if(snapshot.isEmpty()){
                        stopDiagnosticTimeout985();
                        addLine("✗ No Party Room has this code on Firebase server.");''',
'Room not found is a terminal result')

s=once(s,
'''                    if(snapshot.size()>1){
                        addLine("⚠ Multiple rooms share the six-digit code.''',
'''                    if(snapshot.size()>1){
                        stopDiagnosticTimeout985();
                        addLine("⚠ Multiple rooms share the six-digit code.''',
'Ambiguous Room code ends test clearly')

s=once(s,
'''        if(room==null||!room.exists()){
            addLine("✗ Room document does not exist on the server.");return;
        }''',
'''        if(room==null||!room.exists()){
            stopDiagnosticTimeout985();
            addLine("✗ Room document does not exist on the server.");return;
        }''',
'Missing Room does not wait indefinitely')

s=once(s,
'''        if(Boolean.TRUE.equals(room.getBoolean("closed"))){
            addLine("✗ The Host closed this Room.''',
'''        if(Boolean.TRUE.equals(room.getBoolean("closed"))){
            stopDiagnosticTimeout985();
            addLine("✗ The Host closed this Room.''',
'Closed Room stops timeout')

s=once(s,
'''            .addOnSuccessListener(member->{
                if(!current983(gen,uid))return;
                addLine("✓ Room member record: "+''',
'''            .addOnSuccessListener(member->{
                if(!current983(gen,uid))return;
                stopDiagnosticTimeout985();
                addLine("✓ Room member record: "+''',
'Verified member lookup completes the diagnostic')

s=once(s,
'''    @Override protected void onStop(){
        stopMemberWatch984();''',
'''    @Override protected void onStop(){
        stopDiagnosticTimeout985();
        ++generation983;
        stopMemberWatch984();''',
'App background stops diagnostic and invalidates old Firebase callbacks')

s=once(s,
'''    @Override protected void onDestroy(){
        destroyed983=true;''',
'''    @Override protected void onDestroy(){
        stopDiagnosticTimeout985();
        destroyed983=true;''',
'Activity destruction never leaves a scheduled diagnostic timer')
diagnostic.write_text(s)

g=gradle.read_text()
g=once(g,
"versionCode 175; versionName '9.8.4-two-phone-member-sync-watch'",
"versionCode 176; versionName '9.8.5-party-connection-quick-test'",
'Upgrade safely from signed v9.8.4 to v9.8.5')
gradle.write_text(g)
print('PASS v9.8.5 Party Quick Test ready; previous v9.8.4 Firebase member watch unchanged')
