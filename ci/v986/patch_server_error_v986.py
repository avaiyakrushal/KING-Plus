#!/usr/bin/env python3
"""KING Plus v9.8.6 - diagnose server-only failure without trusting a cached room.
Base: successful v9.8.5 source artifact, never tracked outdated /app.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
shutil.copy2(Path(__file__).with_name('KingFirestoreRecovery986.java'),
             pkg/'KingFirestoreRecovery986.java')

def once(text,old,new,name):
    count=text.count(old)
    if count!=1:raise SystemExit(f"{name}: expected exactly 1 marker, got {count}: {old[:100]!r}")
    print("PASS",name)
    return text.replace(old,new,1)

party=pkg/'PartyActivity.java'
s=party.read_text()
s=once(s,
'''                                    showPartyJoinFailure977("Firebase room read still failed after reconnect: "+
                                        msg(err979)+". Try mobile data or check Firebase access.");''',
'''                                    showPartyJoinFailure977(KingFirestoreRecovery986.guidance(
                                        err979 instanceof com.google.firebase.firestore.FirebaseFirestoreException
                                            ?((com.google.firebase.firestore.FirebaseFirestoreException)err979)
                                                .getCode().name():"UNKNOWN",
                                        err979==null?null:err979.getMessage(),
                                        firebaseProject986()));''',
'Show safe actionable Firebase SERVER failure, no cache-based join')

marker='    private void reconnectPartyFirestore979(boolean refreshToken979){'
s=once(s,marker,
'''    private String firebaseProject986(){
        try{
            String id986=com.google.firebase.FirebaseApp.getInstance()
                .getOptions().getProjectId();
            return id986==null?"unknown":id986;
        }catch(Exception ignored){return "unknown";}
    }

'''+marker,
'Identify the actual configured Firebase project on device')
party.write_text(s)

diag=pkg/'KingConnection983Activity.java'
d=diag.read_text()
d=once(d,
'''        addLine("Firebase UID prefix: "+KingConnectionDiagnostics983.uidPrefix(uid));
        if(!KingNetwork.online(this)){''',
'''        addLine("Firebase UID prefix: "+KingConnectionDiagnostics983.uidPrefix(uid));
        String project986="unknown";
        try{
            String configured986=com.google.firebase.FirebaseApp.getInstance()
                .getOptions().getProjectId();
            if(configured986!=null)project986=configured986;
        }catch(Exception ignored){}
        addLine("Firebase project: "+KingConnectionDiagnostics983.safeLine(project986,70));
        if(KingFirestoreRecovery986.projectMismatch(project986)){
            addLine("✗ This APK is configured for the WRONG Firebase project.");
            addLine("Expected: "+KingFirestoreRecovery986.EXPECTED_PROJECT);
            addLine("Room access must not be attempted in a different project.");
            return;
        }
        if(!KingNetwork.online(this)){''',
'On-device Firebase test checks wrong-project APK configuration')

d=once(d,
'''        addLine("✗ "+operation+": "+KingConnectionDiagnostics983.friendlyError(code));
        addLine("The test made no Room writes and charged no money.");''',
'''        addLine("✗ "+operation+": "+KingConnectionDiagnostics983.friendlyError(code));
        if(KingFirestoreRecovery986.cacheOnly(
            error==null?null:error.getMessage()))
            addLine("⚠ LOCAL CACHE is not proof the Room exists on Firebase SERVER.");
        addLine("The test made no Room writes and charged no money.");''',
'Clarify offline cached Room data cannot verify server membership')
diag.write_text(d)

gradle=root/'app/build.gradle'
g=gradle.read_text()
g=once(g,
"versionCode 176; versionName '9.8.5-party-connection-quick-test'",
"versionCode 177; versionName '9.8.6-firestore-server-diagnosis'",
'Move to new signed-compatible v9.8.6 build version')
gradle.write_text(g)
print('PASS v9.8.6 Firebase server diagnostics integrated: no billing, Room writes, or cache bypass')
