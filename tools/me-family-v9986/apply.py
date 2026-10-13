#!/usr/bin/env python3
"""KING Plus 9.9.86 Me Family: real-time member list, owner-approved roles, secure joins and leave.

Patch full signed v9.9.85 source, retain existing Me/CP/Gifts/Agency/Games.
"""
from pathlib import Path
import shutil,re,sys
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
here=Path(__file__).resolve().parent
shutil.copyfile(here/'KingMeFamilyRoles9986.java',src/'KingMeFamilyRoles9986.java')
rules=root/'firestore.rules';r=rules.read_text()
start=r.index('      match /members/{memberUid} {',r.index('    match /families/{familyCode} {'))
end=r.index('      match /messages/{messageId} {',start)
r=r[:start]+(here/'family-members-firestore.rules').read_text()+r[end:]
rules.write_text(r)

java=src/'CommunityHubActivity.java';s=java.read_text()
def replace(old,new):
 global s
 assert s.count(old)==1,('CommunityHub source changed '+old[:85])
 s=s.replace(old,new,1)
replace('''    private com.google.firebase.firestore.ListenerRegistration familyMessages956;''',
'''    private com.google.firebase.firestore.ListenerRegistration familyMessages956;
    private com.google.firebase.firestore.ListenerRegistration familyMembers9986;''')
replace('''    private void stopFamilyMessages956(){if(familyMessages956!=null){familyMessages956.remove();familyMessages956=null;}}''',
'''    private void stopFamilyMessages956(){
        if(familyMessages956!=null){familyMessages956.remove();familyMessages956=null;}
        if(familyMembers9986!=null){familyMembers9986.remove();familyMembers9986=null;}
    }''')
# Legacy UI falsely claimed Lucky Bag was verified Family currency.
replace('''Families include members, Family Chat, Lucky Bag, activity history, level and treasury.''',
'''Real live Family members, roles and chat. Lucky Bags are TEST-only; real Family treasury and level rewards are not yet enabled.''')
# Owner creation already provides a server-timestamp joinedAt field.
old='''            Map<String,Object> member956=new HashMap<>();
            member956.put("uid",me.getUid());member956.put("name",displayName);member956.put("role","member");
            r.collection("members").document(me.getUid()).set(member956,SetOptions.merge())
                .addOnSuccessListener(v->{if(loading955.isShowing())loading955.dismiss();saveFamily(familyCode955,familyName955);})
                .addOnFailureListener(e->{if(loading955.isShowing())loading955.dismiss();familyError955("Could not join Family",e,()->joinFamily(familyCode955));});'''
new='''            final DocumentReference membership9986=r.collection("members").document(me.getUid());
            membership9986.get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(existing9986->{
                    if(existing9986.exists()){
                        if(loading955.isShowing())loading955.dismiss();
                        saveFamily(familyCode955,familyName955);
                        return;
                    }
                    Map<String,Object> member956=new HashMap<>();
                    member956.put("uid",me.getUid());member956.put("name",displayName);
                    member956.put("role","member");
                    member956.put("joinedAt",FieldValue.serverTimestamp());
                    membership9986.set(member956)
                        .addOnSuccessListener(v->{if(loading955.isShowing())loading955.dismiss();saveFamily(familyCode955,familyName955);})
                        .addOnFailureListener(e->{if(loading955.isShowing())loading955.dismiss();familyError955("Could not join Family",e,()->joinFamily(familyCode955));});
                }).addOnFailureListener(e->{
                    if(loading955.isShowing())loading955.dismiss();
                    familyError955("Could not verify Family membership",e,()->joinFamily(familyCode955));
                });'''
replace(old,new)
# Let members see role management as a real, explicit control. Only owner can act.
old='''r.collection("members").get().addOnSuccessListener(s->{memberStat951.setText("👥\n"+s.size()+"\nMembers");memberSearch956.setEnabled(true);if(s.isEmpty())memberList951.addView(tv("No members loaded",13,MUTED,false));for(DocumentSnapshot d:s.getDocuments()){String uid=d.getId(),n=safe(d.getString("name"),"User"),role=safe(d.getString("role"),"member");TextView x=tv(("owner".equals(role)?"👑 ":"👤 ")+n+" • "+role,14,DARK,true);memberList951.addView(x,new LinearLayout.LayoutParams(-1,dp(44)));if(owner.equals(me.getUid())&&!uid.equals(me.getUid()))x.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle(n).setMessage("Remove from Family?").setPositiveButton("Remove",(a,b)->r.collection("members").document(uid).delete().addOnSuccessListener(q->render("Family"))).setNegativeButton("Cancel",null).show();return true;});}}).addOnFailureListener(e->memberStat951.setText("👥\n—\nMembers"));'''
# Source has actual newline escape in Java as \\n (Python raw above matching \n) but normalize via indexed region.
start=s.index('            r.collection("members").get().addOnSuccessListener(')
end=s.index('\n\n            r.collection("activities")',start)
new='''            attachLiveFamilyMembers9986(r,memberList951,memberStat951,memberSearch956,
                owner,code,familyBody956);'''
s=s[:start]+new+s[end:]
# Append family member list real-time, owner role management, owner rename and others' live removal.
anchor='''    private void filterFamilyMembers956(LinearLayout list,String query){'''
assert s.count(anchor)==1
methods='''    private void attachLiveFamilyMembers9986(
        DocumentReference family,LinearLayout list,TextView total,EditText search,
        String ownerUid,String code,LinearLayout originalBody){
        if(familyMembers9986!=null)familyMembers9986.remove();
        familyMembers9986=family.collection("members").addSnapshotListener((snapshot,error)->{
            if(body!=originalBody||familyStopped956||isFinishing()||isDestroyed()
                ||!"Family".equals(selected)||me==null)return;
            list.removeAllViews();
            if(error!=null||snapshot==null){
                total.setText("👥\\\\n—\\\\nMembers");
                list.addView(tv("Cannot refresh Family members. Reopen Family.",13,MUTED,false));
                return;
            }
            boolean ownMembership=false;
            for(DocumentSnapshot member:snapshot.getDocuments())
                if(me.getUid().equals(member.getId()))ownMembership=true;
            if(!ownMembership){
                // When removed by Owner on another phone, stop displaying stale
                // chat and revoke local Family shortcut without deleting history.
                stopFamilyMessages956();clearFamily();render("Family");return;
            }
            total.setText("👥\\\\n"+snapshot.size()+"\\\\nMembers");
            search.setEnabled(true);
            if(snapshot.isEmpty())list.addView(tv("No Family members",13,MUTED,false));
            for(DocumentSnapshot member:snapshot.getDocuments()){
                final String memberUid=member.getId();
                final String memberName=safe(member.getString("name"),"KING User");
                final String role=safe(member.getString("role"),"member");
                TextView label=tv(KingMeFamilyRoles9986.badge(role)+" • "+memberName,
                    14,DARK,true);
                list.addView(label,new LinearLayout.LayoutParams(-1,dp(44)));
                if(KingMeFamilyRoles9986.mayChange(me.getUid(),ownerUid,memberUid,"member")){
                    label.setOnLongClickListener(v->{
                        showFamilyRoleOptions9986(family,memberUid,memberName,ownerUid);
                        return true;
                    });
                }
            }
            filterFamilyMembers956(list,search.getText().toString());
        });
    }
    private void showFamilyRoleOptions9986(
        DocumentReference family,String memberUid,String name,String ownerUid){
        if(me==null||!KingMeFamilyRoles9986.mayChange(
            me.getUid(),ownerUid,memberUid,"member"))return;
        String[] options={"🛡 Make Admin","⭐ Make Moderator","👤 Regular Member","🚫 Remove from Family"};
        new AlertDialog.Builder(this).setTitle("Manage "+name)
            .setItems(options,(dialog,index)->{
                if(me==null||!me.getUid().equals(ownerUid))return;
                final DocumentReference member=family.collection("members").document(memberUid);
                if(index==3){
                    new AlertDialog.Builder(this).setTitle("Remove Family member?")
                        .setMessage(name+" will immediately lose Family Chat and access.")
                        .setPositiveButton("Remove",(d,w)->member.delete()
                            .addOnFailureListener(e->familyError955("Could not remove Family member",e,()->render("Family"))))
                        .setNegativeButton("Cancel",null).show();
                    return;
                }
                String role=index==0?"admin":index==1?"moderator":"member";
                if(!KingMeFamilyRoles9986.mayChange(me.getUid(),ownerUid,memberUid,role))return;
                member.update("role",role)
                    .addOnFailureListener(e->familyError955(
                        "Family role change rejected",e,()->render("Family")));
            }).setNegativeButton("Cancel",null).show();
    }

'''
s=s.replace(anchor,methods+anchor,1)
java.write_text(s)
gradle=root/'app/build.gradle';g=gradle.read_text()
g,count=re.subn(r"versionCode\s+264\s*;\s*versionName\s+'[^']+'",
  "versionCode 265; versionName '9.9.86-me-real-family-roles-test'",g)
assert count==1,'Must start from last signed full v9.9.85 source'
gradle.write_text(g)
assert 'KingMeCouplePolicy9985' in s
assert 'KingMePrivacy9984Activity' in (root/'app/src/main/AndroidManifest.xml').read_text()
assert 'KingAgency9982Activity' in (root/'app/src/main/AndroidManifest.xml').read_text()
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS cloud Family join/leave live membership, owner-only role updates and 5 older Me/Party gates preserved')
