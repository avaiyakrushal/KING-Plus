#!/usr/bin/env python3
"""KING 9.9.85: genuine consenting CP, real cloud Me profile count and honest Family status.

Applies on complete v9.9.84 signed source, retaining all previous features.
"""
from pathlib import Path
import shutil,sys,re
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
here=Path(__file__).resolve().parent
shutil.copyfile(here/'KingMeCouplePolicy9985.java',src/'KingMeCouplePolicy9985.java')
rules=root/'firestore.rules'
r=rules.read_text()
begin=r.index('    match /relationships/{relationshipId} {')
end=r.index('    match /families/{familyCode} {',begin)
r=r[:begin]+(here/'relationship-firestore.rules').read_text()+"\n"+r[end:]
# Family owner can't arbitrarily mint "Treasury" or boost Level / membership.
old="""      allow update, delete: if familyOwner(familyCode);

      match /members/{memberUid} {"""
new="""      allow update: if familyOwner(familyCode)
        && request.resource.data.ownerUid == resource.data.ownerUid
        && request.resource.data.code == resource.data.code
        && request.resource.data.createdAt == resource.data.createdAt
        && request.resource.data.level == resource.data.level
        && request.resource.data.treasury == resource.data.treasury
        && request.resource.data.memberCount == resource.data.memberCount;
      allow delete: if familyOwner(familyCode);

      match /members/{memberUid} {"""
assert r.count(old)==1,'Family rules boundary changed'
r=r.replace(old,new,1)
old="""        && request.resource.data.name.size() <= 120;
      allow update:"""
new="""        && request.resource.data.name.size() <= 120
        && request.resource.data.level == 1
        && request.resource.data.treasury == 0
        && request.resource.data.memberCount == 1
        && request.resource.data.createdAt == request.time
        && request.resource.data.updatedAt == request.time;
      allow update:"""
assert r.count(old)==1,'Family create boundary changed'
r=r.replace(old,new,1)
rules.write_text(r)

community=src/'CommunityHubActivity.java'
c=community.read_text()
old='''    private String newCode(){String a="ABCDEFGHJKLMNPQRSTUVWXYZ23456789";long x=Math.abs((me.getUid()+System.nanoTime()).hashCode()*2654435761L);StringBuilder b=new StringBuilder();for(int i=0;i<6;i++){b.append(a.charAt((int)(x%a.length())));x=x/a.length()+31;}return b.toString();}'''
new='''    private String newCode(){
        String alphabet="ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        java.security.SecureRandom rng=new java.security.SecureRandom();
        StringBuilder code=new StringBuilder(6);
        for(int i=0;i<6;i++)code.append(alphabet.charAt(rng.nextInt(alphabet.length())));
        return code.toString();
    }'''
assert c.count(old)==1,'Family code generator changed'
c=c.replace(old,new,1)
old='''    private String pairId(String a,String b){return a.compareTo(b)<0?a+"__"+b:b+"__"+a;}'''
assert c.count(old)==1
c=c.replace(old,'''    private String pairId(String a,String b){return KingMeCouplePolicy9985.pairId(a,b);}''',1)
# Avoid writing a misleading local CP name or fake success on declined settlement
old='''if("Accept".equals(op))getSharedPreferences("MainActivity",MODE_PRIVATE).edit().putString("coupling_name",name).apply();render("Relationship");'''
assert c.count(old)==1
c=c.replace(old,'''render("Relationship");''',1)
old='''getSharedPreferences("MainActivity",MODE_PRIVATE).edit().remove("coupling_name").apply();render("Relationship");'''
assert c.count(old)==1
c=c.replace(old,'''render("Relationship");''',1)
# Family counters must not pretend to be wallet balances.
old='''familyStat951("💎",String.valueOf(treasury),"Treasury")'''
assert c.count(old)==1
c=c.replace(old,'''familyStat951("◇","—","Unverified")''',1)
community.write_text(c)
main=src/'MainActivity.java'
m=main.read_text()
old='''String coupling=getPreferences(0).getString("coupling_name","");addProfileStat(counts,coupling.isEmpty()?"0":"1","Coupling",()->openCommunityHub700("Relationship"));'''
new='''TextView realCoupling9985=addProfileStat(counts,"…","Coupling",()->openCommunityHub700("Relationship"));loadRealCoupleCount9985(realCoupling9985);'''
assert m.count(old)==1,'Me Profile CP counter changed'
m=m.replace(old,new,1)
old='''    private void couplingPage(){
        screen="coupling"; base("Coupling","KING Plus connection");
        String current=getPreferences(0).getString("coupling_name","");
        text(current.isEmpty()?"No coupling yet":"💞 Coupled with "+current,20,Color.WHITE,true);
        button(current.isEmpty()?"Set Coupling":"Change Coupling",PURPLE,()->{
            final EditText e=new EditText(this); e.setHint("User name");
            new AlertDialog.Builder(this).setTitle("Coupling").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{ String n=e.getText().toString().trim(); if(!n.isEmpty()) getPreferences(0).edit().putString("coupling_name",n).apply(); couplingPage(); }).show();
        });
        if(!current.isEmpty()) button("Remove Coupling",CARD,()->{getPreferences(0).edit().remove("coupling_name").apply();couplingPage();});
        button("Back to Me",CARD,this::profile);
    }'''
new='''    private void couplingPage(){
        // CP exists only after the other person accepts the Firebase request.
        // A typed name on one phone is not proof of a relationship.
        openCommunityHub700("Relationship");
    }

    private void loadRealCoupleCount9985(TextView counter){
        if(counter==null||firestore==null||firebaseAuth==null||
           firebaseAuth.getCurrentUser()==null){if(counter!=null)counter.setText("—");return;}
        final String account9985=firebaseAuth.getCurrentUser().getUid();
        final java.util.List<String[]> pairs9985=new java.util.ArrayList<>();
        final int[] remaining9985={2};
        final boolean[] unavailable9985={false};
        Runnable finish9985=()->{
            remaining9985[0]--;
            if(remaining9985[0]!=0||isFinishing()||isDestroyed()||
               firebaseAuth==null||firebaseAuth.getCurrentUser()==null||
               !account9985.equals(firebaseAuth.getCurrentUser().getUid()))return;
            if(unavailable9985[0])counter.setText("—");
            else counter.setText(String.valueOf(
                KingMeCouplePolicy9985.countAccepted(account9985,pairs9985)));
        };
        java.util.function.Consumer<com.google.firebase.firestore.QuerySnapshot> take9985=snapshot->{
            if(snapshot!=null)for(DocumentSnapshot item:snapshot.getDocuments()){
                String a=item.getString("uidA"),b=item.getString("uidB");
                String status=item.getString("status");
                pairs9985.add(new String[]{a,b,status});
            }
            finish9985.run();
        };
        firestore.collection("relationships").whereEqualTo("uidA",account9985).limit(100)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(take9985::accept)
          .addOnFailureListener(e->{unavailable9985[0]=true;finish9985.run();});
        firestore.collection("relationships").whereEqualTo("uidB",account9985).limit(100)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(take9985::accept)
          .addOnFailureListener(e->{unavailable9985[0]=true;finish9985.run();});
    }'''
assert m.count(old)==1,'Local-only CP edit screen changed'
m=m.replace(old,new,1)
main.write_text(m)
gradle=root/'app/build.gradle';g=gradle.read_text()
g,n=re.subn(r"versionCode\s+263\s*;\s*versionName\s+'[^']+'",
  "versionCode 264; versionName '9.9.85-me-real-relationship-family-test'",g)
assert n==1,'Restore signed v9.9.84 source'
gradle.write_text(g)
assert 'KingMePrivacy9984Activity' in (root/'app/src/main/AndroidManifest.xml').read_text()
assert 'KingLiveBingo9980Activity' in (root/'app/src/main/AndroidManifest.xml').read_text()
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS Family code secure RNG + freeze spoofed treasury + reciprocal CP consent + real Profile signed-in CP count')
