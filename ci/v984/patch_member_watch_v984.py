#!/usr/bin/env python3
"""KING Plus v9.8.4: opt-in read-only realtime Party member watch on phone.

Start from verified v9.8.3 APK source artifact, not obsolete repository /app.
Show a realtime Firestore *member-document* count on both phones, with
a clear distinction between cached and server-confirmed snapshots, safe UID
prefixes and per-room/account/listener generation guards.

A read-only watch NEVER joins, creates, deletes or changes a Party. It only
helps pinpoint why users on separate phones do not see each other.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
shutil.copy2(Path(__file__).with_name('KingLiveMembers984.java'),pkg/'KingLiveMembers984.java')
f=pkg/'KingConnection983Activity.java'
s=f.read_text()

def change(old,new,label):
    global s
    n=s.count(old)
    if n!=1:
        raise SystemExit(f'{label}: expected one marker, found {n}: {old[:120]!r}')
    s=s.replace(old,new,1)
    print('PASS',label)

change('import com.google.firebase.firestore.Source;',
'''import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;''',
'Import Firestore realtime snapshot and lifecycle handles')

change('    private boolean destroyed983;',
'''    private boolean destroyed983;
    private DocumentReference inspectedRoom984;
    private String inspectedUid984;
    private int inspectedGeneration984;
    private ListenerRegistration watchListener984;
    private int watchSession984;
    private TextView liveStatus984;
    private Button watchButton984;''',
'Maintain room/UID-bound read-only member watch')

change('''        Button test=button(column,"🔍 Run Firebase / Party Connection Test");
        Button copy=button(column,"📋 Copy Test Report");''',
'''        Button test=button(column,"🔍 Run Firebase / Party Connection Test");
        watchButton984=button(column,"👥 Watch Party Members (Live)");
        watchButton984.setEnabled(false);
        Button copy=button(column,"📋 Copy Test Report");''',
'Show opt-in live member monitor only after verified Room lookup')

change('''        column.addView(output);

        test.setOnClickListener(v->runTest983());''',
'''        column.addView(output);
        liveStatus984=paragraph(
            "Realtime member watch has not started. Run the Room test first.",
            13,0xffabccea);
        liveStatus984.setTextIsSelectable(true);
        column.addView(liveStatus984);

        test.setOnClickListener(v->runTest983());
        watchButton984.setOnClickListener(v->startMemberWatch984());''',
'Allow user to opt into realtime member watch only when ready')

change('''clipboard.setPrimaryClip(ClipData.newPlainText("KING Plus Firebase test",report.toString()));''',
'''String watch984=liveStatus984==null?"not started":
                    KingConnectionDiagnostics983.safeLine(liveStatus984.getText().toString(),500);
                clipboard.setPrimaryClip(ClipData.newPlainText(
                    "KING Plus Firebase test",
                    report.toString()+"\\nRead-only live members: "+watch984));''',
'Include server/cached member result in redacted copied support report')

change('''    private void runTest983(){
        generation983++;
        final int gen=generation983;''',
'''    private void runTest983(){
        generation983++;
        stopMemberWatch984();
        inspectedRoom984=null;
        inspectedUid984=null;
        inspectedGeneration984=0;
        watchButton984.setEnabled(false);
        liveStatus984.setText("Run the Room test, then tap Watch Party Members.");
        final int gen=generation983;''',
'Invalidate member listener whenever a new connection test starts')

change('''        addLine("4. Checking whether this Firebase UID is already a room member…");''',
'''        // Only rooms confirmed by the Firebase SERVER are available to watch.
        inspectedRoom984=room.getReference();
        inspectedUid984=uid;
        inspectedGeneration984=gen;
        watchButton984.setEnabled(true);
        addLine("4. Checking whether this Firebase UID is already a room member…");''',
'Make watch available only for server-resolved and nonclosed Party Room')

change('''    @Override protected void onDestroy(){
        destroyed983=true;
        ++generation983;
        super.onDestroy();
    }''',
r'''    private void stopMemberWatch984(){
        ++watchSession984;
        if(watchListener984!=null){
            watchListener984.remove();
            watchListener984=null;
        }
    }

    private void startMemberWatch984(){
        final DocumentReference room984=inspectedRoom984;
        final String uid984=inspectedUid984;
        final int testGeneration984=inspectedGeneration984;
        if(room984==null||uid984==null||!current983(testGeneration984,uid984)){
            liveStatus984.setText("First run the Firebase Room Test with a valid Room Code.");
            return;
        }
        stopMemberWatch984();
        final int watchGeneration984=watchSession984;
        final String roomId984=room984.getId();
        liveStatus984.setText(
            "Listening for Firestore member records… Cached data will NOT count as server proof.");
        try{
            watchListener984=room984.collection("members")
                .addSnapshotListener(MetadataChanges.INCLUDE,(snapshot984,error984)->{
                    if(watchGeneration984!=watchSession984
                        || !KingLiveMembers984.active(
                            testGeneration984,generation983,
                            uid984,currentUid983(),roomId984,
                            inspectedRoom984==null?null:inspectedRoom984.getId(),
                            !destroyed983&&!isFinishing()&&!isDestroyed()))return;
                    if(error984!=null){
                        String errorCode984="UNKNOWN";
                        if(error984 instanceof FirebaseFirestoreException)
                            errorCode984=((FirebaseFirestoreException)error984).getCode().name();
                        liveStatus984.setText("✗ Realtime member listener: "+
                            KingConnectionDiagnostics983.friendlyError(errorCode984));
                        return;
                    }
                    if(snapshot984==null){
                        liveStatus984.setText("No member snapshot received yet.");
                        return;
                    }
                    boolean cached984=snapshot984.getMetadata().isFromCache();
                    boolean ownMember984=false;
                    java.util.ArrayList<String> ids984=new java.util.ArrayList<>();
                    for(DocumentSnapshot member984:snapshot984.getDocuments()){
                        String id984=member984.getId();
                        if(uid984.equals(id984))ownMember984=true;
                        if(ids984.size()<8)ids984.add(id984);
                    }
                    // These are member *records*. A stale/crashed user can still
                    // have a record; this is not a count of active voice callers.
                    String result984=KingLiveMembers984.state(
                        snapshot984.size(),ownMember984,cached984);
                    liveStatus984.setText("👥 "+result984+
                        "\nMember UID prefixes (max 8): "+
                        KingLiveMembers984.samplePrefixes(ids984,8)+
                        "\nMember records may include offline users. This test never joins.");
                });
        }catch(Exception error984){
            liveStatus984.setText(
                "Could not start read-only member listener. Check Firebase connectivity.");
        }
    }

    @Override protected void onStop(){
        stopMemberWatch984();
        super.onStop();
    }

    @Override protected void onDestroy(){
        destroyed983=true;
        ++generation983;
        stopMemberWatch984();
        super.onDestroy();
    }''',
'Attach read-only server-aware live member listener, remove it on stop and reject stale callbacks')

f.write_text(s)
gradle=root/'app/build.gradle'
g=gradle.read_text()
old="versionCode 174; versionName '9.8.3-on-phone-firebase-connection-test'"
if g.count(old)!=1:raise SystemExit('Expected successful v9.8.3 source baseline')
gradle.write_text(g.replace(
    old,"versionCode 175; versionName '9.8.4-two-phone-member-sync-watch'",1))
print('PASS KING Plus v9.8.4 Android code prepared: opt-in Firestore member watch, no writes or billing')
