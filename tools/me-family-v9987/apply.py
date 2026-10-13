#!/usr/bin/env python3
"""KING v9.9.87 – REAL live Family roster & usable Admin/Moderator moderation.

Only apply against complete signed v9.9.86 Family roles source. Never mint Wallet
credits, paid recharge, VIP/Noble or unverifiable Family Treasury.
"""
from pathlib import Path
import re,shutil,sys
r=Path(sys.argv[1])
here=Path(__file__).parent
src=r/'app/src/main/java/com/kingplus/social'
shutil.copyfile(here/'KingFamilyModeration9987.java',src/'KingFamilyModeration9987.java')
rules=r/'firestore.rules'
s=rules.read_text()
family=s.index('    match /families/{familyCode} {')
anchor='''    match /families/{familyCode} {'''
assert s.count(anchor)==1
s=s.replace(anchor,(here/'family-moderation-helpers.rules').read_text()+'\n'+anchor,1)
family=s.index(anchor)
start=s.index('        allow delete: if signedIn()',s.index('      match /members/{memberUid} {',family))
end=s.index('      }',start)
old=s[start:end]
assert 'memberUid == request.auth.uid || familyOwner(familyCode)' in old
s=s[:start]+'''        allow delete: if signedIn()
          && memberUid != familyData(familyCode).ownerUid
          && (memberUid == request.auth.uid || familyOwner(familyCode)
            || (familyAdminCanRemove9987(familyCode)
              && memberUid != request.auth.uid
              && resource.data.role == 'member'));
'''+s[end:]
start=s.index('      match /messages/{messageId} {',family)
end=s.index('      match /treasury/{entryId} {',start)
s=s[:start]+'''      match /messages/{messageId} {
        allow read: if familyMember(familyCode);
        // Membership and real server timestamp required. Prevent arbitrary
        // status/reward metadata injected through Family Chat.
        allow create: if familyMember(familyCode)
          && request.resource.data.keys().hasOnly(['uid','name','text','createdAt'])
          && request.resource.data.keys().hasAll(['uid','name','text','createdAt'])
          && request.resource.data.uid == request.auth.uid
          && request.resource.data.name is string
          && request.resource.data.name.size() > 0
          && request.resource.data.name.size() <= 80
          && request.resource.data.text is string
          && request.resource.data.text.size() > 0
          && request.resource.data.text.size() <= 1000
          && request.resource.data.createdAt == request.time;
        allow update: if false;
        allow delete: if signedIn() && (
          resource.data.uid == request.auth.uid
          || familyChatModerator9987(familyCode));
      }

'''+s[end:]
# Family member treasury is TEST-only; no client may forge a real balance.
# The actual Family Lucky Bag is separately labelled TEST and does not write
# to /treasury. Deny speculative treasury deposits until trusted ledger exists.
old='''        allow create: if familyMember(familyCode)
          && request.resource.data.uid == request.auth.uid
          && request.resource.data.amount is int
          && request.resource.data.amount > 0
          && request.resource.data.amount <= 10000;'''
assert s.count(old)==1
s=s.replace(old,'''        allow create: if false; // NO real currency without trusted backend''',1)
rules.write_text(s)
f=src/'CommunityHubActivity.java'
x=f.read_text()
def change(old,new):
 global x
 assert x.count(old)==1,old[:100]
 x=x.replace(old,new,1)
change('''    private com.google.firebase.firestore.ListenerRegistration familyMembers9986;''',
'''    private com.google.firebase.firestore.ListenerRegistration familyMembers9986;
    private String realFamilyRole9987="member";
    private void stopFamilyChatOnly9987(){
        if(familyMessages956!=null){familyMessages956.remove();familyMessages956=null;}
    }''')
change('''            stopFamilyMessages956();
            if(!familyStopped956&&!isFinishing()''',
'''            // Do NOT detach familyMembers9986 here. Old code cancelled the
            // real-time member listener just after attaching it.
            stopFamilyChatOnly9987();
            if(!familyStopped956&&!isFinishing()''')
change('''            boolean ownMembership=false;
            for(DocumentSnapshot member:snapshot.getDocuments())
                if(me.getUid().equals(member.getId()))ownMembership=true;''',
'''            boolean ownMembership=false;
            realFamilyRole9987="member";
            for(DocumentSnapshot member:snapshot.getDocuments()){
                if(me.getUid().equals(member.getId())){
                    ownMembership=true;
                    realFamilyRole9987=safe(member.getString("role"),"member");
                }
            }''')
change('''                if(KingMeFamilyRoles9986.mayChange(me.getUid(),ownerUid,memberUid,"member")){
                    label.setOnLongClickListener(v->{
                        showFamilyRoleOptions9986(family,memberUid,memberName,ownerUid);
                        return true;
                    });
                }''',
'''                if(KingMeFamilyRoles9986.mayChange(me.getUid(),ownerUid,memberUid,"member")){
                    label.setOnLongClickListener(v->{
                        showFamilyRoleOptions9986(family,memberUid,memberName,ownerUid);
                        return true;
                    });
                }else if(KingFamilyModeration9987.canRemoveMember(me.getUid(),ownerUid,
                        realFamilyRole9987,memberUid,role)){
                    label.setOnLongClickListener(v->{
                        new AlertDialog.Builder(this).setTitle("Admin • Remove Member")
                            .setMessage("Remove "+memberName+" from the Family? Admins cannot remove moderators, admins or owners.")
                            .setPositiveButton("Remove",(d,w)->family.collection("members").document(memberUid)
                                .delete().addOnFailureListener(e->toast("Family admin removal denied")))
                            .setNegativeButton("Cancel",null).show();
                        return true;
                    });
                }''')
# Each real Firebase Chat message gets role-aware moderator deletion.
old='''                for(DocumentSnapshot message956:messages956)chat.addView(tv(safe(message956.getString("name"),"User")+": "+safe(message956.getString("text"),""),13,DARK,false));'''
new='''                for(DocumentSnapshot message956:messages956){
                    String senderUid9987=safe(message956.getString("uid"),"");
                    TextView messageView9987=tv(
                        safe(message956.getString("name"),"User")+": "+
                        safe(message956.getString("text"),""),13,DARK,false);
                    chat.addView(messageView9987);
                    if(KingFamilyModeration9987.canModerateMessage(
                        me.getUid(),owner,realFamilyRole9987,senderUid9987)){
                        messageView9987.setOnLongClickListener(v->{
                            new AlertDialog.Builder(this).setTitle("Moderate Family Chat")
                                .setMessage("Delete this message for all Family members?")
                                .setPositiveButton("Delete",(dialog,which)->message956.getReference()
                                    .delete().addOnFailureListener(error->toast("Message deletion denied")))
                                .setNegativeButton("Cancel",null).show();
                            return true;
                        });
                    }
                }'''
change(old,new)
# Functional owner Family name editor; does NOT modify Level/Treasury.
anchor='''            body.addView(acts);
            TextView luckyBag951'''
replacement='''            if(owner.equals(me.getUid())){
                TextView rename9987=button("✏ Edit Family Name",()->{
                    EditText renameInput9987=new EditText(this);
                    renameInput9987.setSingleLine(true);
                    renameInput9987.setText(name);
                    new AlertDialog.Builder(this).setTitle("Rename Family")
                        .setView(renameInput9987)
                        .setPositiveButton("Save",(dialog,which)->{
                            String renamed=renameInput9987.getText().toString().trim();
                            if(renamed.length()<2||renamed.length()>80){toast("Family name: 2–80 characters");return;}
                            Map<String,Object> updates=new HashMap<>();
                            updates.put("name",renamed);
                            updates.put("updatedAt",FieldValue.serverTimestamp());
                            r.update(updates).addOnSuccessListener(v->render("Family"))
                                .addOnFailureListener(e->toast("Family rename rejected"));
                        }).setNegativeButton("Cancel",null).show();
                });
                acts.addView(rename9987,new LinearLayout.LayoutParams(0,dp(46),1));
            }
            body.addView(acts);
            TextView luckyBag951'''
change(anchor,replacement)
f.write_text(x)
gradle=r/'app/build.gradle'
g=gradle.read_text()
g,count=re.subn(r"versionCode\s+265\s*;\s*versionName\s+'[^']+'",
  "versionCode 266; versionName '9.9.87-me-live-family-moderation-test'",g)
assert count==1,'Expected final signed 9.9.86 Android source'
gradle.write_text(g)
assert 'KingMeFamilyRoles9986' in x and 'KingMeCouplePolicy9985' in x
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print("PASS Family live member listener retained, Admin kick, Moderator chat deletion, owner Family rename, no bogus treasury deposits")
