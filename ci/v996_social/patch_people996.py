#!/usr/bin/env python3
from pathlib import Path
import sys
f=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social/SocialActivity.java'
s=f.read_text()
def once(a,b,label):
 global s
 n=s.count(a)
 if n!=1:raise RuntimeError(label+' '+str(n))
 s=s.replace(a,b,1)
 print('PASS',label)
once('''            publishOwnProfile();
            loadDiscover();''',
'''            verifyOwnProfile996();
            loadDiscover();''','verify new people profiles')

helper=r'''    private void verifyOwnProfile996(){
        if(!currentAccount970())return;
        final String uid=me.getUid();
        final String pid=publicId(uid);
        Map<String,Object> info=new HashMap<>();
        info.put("uid",uid);info.put("publicId",pid);
        // Merge-only. Do not overwrite existing name/photo or private info.
        db.collection("public_profiles").document(uid)
            .set(info,com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener(v->
                db.collection("public_profiles").document(uid)
                    .get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener(doc->{
                        if(!currentAccount970())return;
                        if(doc.exists()&&uid.equals(doc.getString("uid"))
                            &&pid.equals(doc.getString("publicId")))
                            status.setText("✅ KING ID "+pid+" confirmed on Firebase SERVER");
                        else status.setText("⚠ Profile not confirmed on SERVER");
                    })
                    .addOnFailureListener(e->{
                        if(currentAccount970())status.setText(
                            "⚠ Profile server check failed: "+safe(e.getLocalizedMessage()));
                    })
            ).addOnFailureListener(e->{
                if(currentAccount970())status.setText(
                    "⚠ Cannot register KING ID: "+safe(e.getLocalizedMessage()));
            });
    }
    private void connectionTest996(){
        if(!cloudReady()){toast("Sign in to Firebase first");return;}
        final String uid=me.getUid();
        String project="unknown";
        try{project=FirebaseAuth.getInstance().getApp().getOptions().getProjectId();}
        catch(Exception ignored){}
        final String projectId=project;
        db.collection("public_profiles").document(uid)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnCompleteListener(done->{
              boolean found=done.isSuccessful()&&done.getResult()!=null&&done.getResult().exists();
              String detail=found?"Profile FOUND on SERVER":"Profile MISSING or denied on SERVER";
              if(!done.isSuccessful()&&done.getException()!=null)
                  detail+="\n"+safe(done.getException().getLocalizedMessage());
              String message="Firebase project: "+projectId+
                  "\nKING ID: "+publicId(uid)+"\nAccount UID: "+uid+
                  "\n"+detail+
                  "\n\nOn both phones check SAME project and DIFFERENT UIDs. "+
                  "If PERMISSION_DENIED, secure Firestore Rules need publishing in Firebase Console.";
              if(isFinishing()||isDestroyed())return;
              new AlertDialog.Builder(this).setTitle("KING Plus connection test")
                  .setMessage(message)
                  .setPositiveButton("Retry profile registration",(d,w)->verifyOwnProfile996())
                  .setNegativeButton("Close",null).show();
          });
    }

''';
once('''    private void publishOwnProfile() {''',helper+'''    private void publishOwnProfile() {''','add actual server profile diagnostics');
once('''            addQuick(socialActions992,"🎉  Join\nParty ID",this::socialJoinParty992);''',
'''            addQuick(socialActions992,"🎉  Join\nParty ID",this::socialJoinParty992);
            addQuick(socialActions992,"🛠  Test\nConnection",this::connectionTest996);''',
'diagnostic button on People page')
start=s.index('    private void runSearch() {')
end=s.index('    private void loadDiscover() {',start)
block=s[start:end]
assert block.count('.limit(15).get()')==1
assert block.count('.document(raw).get()')==1
assert block.count('.limit(30).get()')==1
block=block.replace('.limit(15).get()', '.limit(15).get(com.google.firebase.firestore.Source.SERVER)')
block=block.replace('.document(raw).get()', '.document(raw).get(com.google.firebase.firestore.Source.SERVER)')
block=block.replace('.limit(30).get()', '.limit(30).get(com.google.firebase.firestore.Source.SERVER)')
block=block.replace('No verified user found with that ID','No synced user found. Ask friend to update KING Plus and open People > Test Connection')
s=s[:start]+block+s[end:]
f.write_text(s)
print('PASS server-only search: ID, UID, name; no stale cached users')
