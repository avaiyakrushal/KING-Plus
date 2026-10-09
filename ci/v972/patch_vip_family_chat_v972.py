#!/usr/bin/env python3
"""KING Plus v9.7.2 – Family realtime membership, verified direct Chat gifts,
real recipient lookup, and VIP server-entitlement-aware display.

Input is successful v9.7.1 built source (preserves gifts + live emoji fixes).
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
shutil.copy2(Path(__file__).with_name('KingFamilyChat972.java'),
             pkg/'KingFamilyChat972.java')

def replace_java_method(s,marker,replacement,label):
    at=s.find(marker)
    if at<0 or s.count(marker)!=1:raise SystemExit(f'{label}: expected unique method signature; found {s.count(marker)}')
    start=s.find('{',at)
    if start<0:raise SystemExit(label+': no method body')
    depth=0;quote=None;escape=False;end=-1
    for i in range(start,len(s)):
        ch=s[i]
        if quote:
            if escape:escape=False
            elif ch=='\\':escape=True
            elif ch==quote:quote=None
        elif ch in ("'",'"'):quote=ch
        elif ch=='{':depth+=1
        elif ch=='}':
            depth-=1
            if depth==0:end=i+1;break
    if end<0:raise SystemExit(label+': unbalanced method')
    print('PASS',label)
    return s[:at]+replacement+s[end:]

def exact(s,a,b,label):
    n=s.count(a)
    if n!=1:raise SystemExit(f'{label}: marker count {n} not 1: {a[:135]!r}')
    print('PASS',label)
    return s.replace(a,b,1)

# ----------------------------------------------------------------------
# Private Chat: never spend local TEST coins to fabricate a cloud Gift.
# Only post a gift message following confirmed Cloud Function wallet success.
# ----------------------------------------------------------------------
f=pkg/'ChatActivity.java';s=f.read_text()
s=replace_java_method(s,'    private boolean sendDirectGift620(',
r'''    private boolean pendingDirectGift972;
    private boolean sendDirectGift620(String name,String icon,int unit,int qty,int total){
        if(isBlocked()){
            Toast.makeText(this,"Unblock this user before sending a gift",Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!cloudMode){
            // Offline/demo messages never change real coins, VIP status or remote gifts.
            String test972=icon+" "+name+" x"+qty+" • TEST preview, not a real gift";
            saveLocal(test972,"text","",true);
            Toast.makeText(this,"TEST preview only • no real Gift or VIP progress",Toast.LENGTH_LONG).show();
            return true;
        }
        if(!KingFamilyChat972.validDirectGift(me==null?null:me.getUid(),
                peerUid,cloudMode,unit,qty,total)){
            Toast.makeText(this,"Sign in and select a verified recipient. Secure gift total must be at most 100,000 coins.",Toast.LENGTH_LONG).show();
            return false;
        }
        if(pendingDirectGift972){
            Toast.makeText(this,"Waiting for secure Gift confirmation",Toast.LENGTH_SHORT).show();
            return false;
        }
        pendingDirectGift972=true;
        final String senderUid972=me.getUid(),recipientUid972=peerUid;
        final String senderName972=myName;
        final String recipientName972=peerName;
        final String giftName972=name,icon972=icon;
        CloudBackend.sendGift(recipientUid972,name+" x"+qty,total,(ok,message)->runOnUiThread(()->{
            pendingDirectGift972=false;
            if(isFinishing()||isDestroyed())return;
            if(!ok){
                Toast.makeText(this,"Gift NOT sent • server wallet unchanged: "+message,Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this,message,Toast.LENGTH_LONG).show();
            // Only the verified server transaction can award local progress.
            LevelSystem.Snapshot progress972=LevelSystem.gift(this,total);
            syncDirectProgress700(progress972);
            if(me==null||!senderUid972.equals(me.getUid())
               ||!recipientUid972.equals(peerUid)||!cloudMode)return;
            Map<String,Object> thread972=new HashMap<>();
            List<String> members972=new ArrayList<>();
            members972.add(senderUid972);members972.add(recipientUid972);
            thread972.put("members",members972);
            Map<String,Object> names972=new HashMap<>();
            names972.put(senderUid972,senderName972);
            names972.put(recipientUid972,recipientName972);
            thread972.put("memberNames",names972);
            thread972.put("lastMessage","🎁 "+giftName972+" x"+qty);
            thread972.put("lastSenderUid",senderUid972);
            thread972.put("updatedAt",FieldValue.serverTimestamp());
            db.collection("direct_threads").document(chatId)
                .set(thread972,SetOptions.merge()).addOnSuccessListener(v->{
                    Map<String,Object> gift972=new HashMap<>();
                    gift972.put("senderUid",senderUid972);
                    gift972.put("recipientUid",recipientUid972);
                    gift972.put("senderName",senderName972);
                    gift972.put("senderLevel",progress972.level);
                    gift972.put("senderVip",progress972.vipLevel);
                    gift972.put("text",icon972+" "+giftName972+" x"+qty+" • "+total+" coins");
                    gift972.put("type","gift");
                    gift972.put("giftName",giftName972);
                    gift972.put("giftIcon",icon972);
                    gift972.put("giftQty",qty);
                    gift972.put("giftUnitCost",unit);
                    gift972.put("giftValue",total);
                    gift972.put("createdAt",FieldValue.serverTimestamp());
                    db.collection("direct_threads").document(chatId)
                        .collection("messages").add(gift972)
                        .addOnFailureListener(error->
                            Toast.makeText(this,
                                "Gift was paid securely, but Chat history could not sync. Check Wallet history.",
                                Toast.LENGTH_LONG).show());
                }).addOnFailureListener(error->
                    Toast.makeText(this,
                        "Gift was paid securely, but Chat thread could not sync. Check Wallet history.",
                        Toast.LENGTH_LONG).show());
        }));
        return true; // close picker while server confirms asynchronously
    }''','private Chat gift debits only the real server wallet and never rewards TEST coins')
s=exact(s,
 'TextView bal=label("💎 "+testCoins,13,0xffffdf6b,true);',
 'TextView bal=label(cloudMode?"💎 Secure wallet":"💎 TEST preview",13,0xffffdf6b,true);',
 'direct gift picker clearly distinguishes secure server Gift from TEST preview')
f.write_text(s)

# ----------------------------------------------------------------------
# Family: live members should appear/disappear across phones without reopen.
# Existing Family Chat already has live snapshots. Retain chat listener.
# ----------------------------------------------------------------------
f=pkg/'CommunityHubActivity.java';s=f.read_text()
s=exact(s,
 '    private com.google.firebase.firestore.ListenerRegistration familyMessages956;',
 '''    private com.google.firebase.firestore.ListenerRegistration familyMessages956;
    private com.google.firebase.firestore.ListenerRegistration familyMembers972;''',
 'add one live Family-member listener and prevent duplicate subscriptions')
s=exact(s,
 '    private void stopFamilyMessages956(){if(familyMessages956!=null){familyMessages956.remove();familyMessages956=null;}}',
 '''    private void stopFamilyMessages956(){
        if(familyMessages956!=null){familyMessages956.remove();familyMessages956=null;}
        if(familyMembers972!=null){familyMembers972.remove();familyMembers972=null;}
    }''',
 'unsubscribe Family member and message listeners on tabs/pause/destroy')
start='            r.collection("members").get().addOnSuccessListener(s->{'
end='            r.collection("activities").orderBy("createdAt"'
a=s.find(start)
b=s.find(end,a)
if a<0 or b<0 or s.count(start)!=1:raise SystemExit('Family member list one-shot source boundaries missing')
s=s[:a]+r'''            familyMembers972=r.collection("members").addSnapshotListener((snap972,error972)->{
                if(body!=familyBody956||familyStopped956||isFinishing()||isDestroyed()||
                    !"Family".equals(selected))return;
                memberList951.removeAllViews();
                if(error972!=null||snap972==null){
                    memberStat951.setText("👥\n—\nMembers");
                    memberList951.addView(tv("Members unavailable • check membership and network",13,MUTED,false));
                    return;
                }
                memberStat951.setText("👥\n"+snap972.size()+"\nMembers");
                memberSearch956.setEnabled(true);
                if(snap972.isEmpty())memberList951.addView(tv("No current members",13,MUTED,false));
                for(DocumentSnapshot member972:snap972.getDocuments()){
                    String uid972=member972.getId();
                    String name972=safe(member972.getString("name"),"User");
                    String role972=safe(member972.getString("role"),"member");
                    TextView card972=tv(("owner".equals(role972)?"👑 ":"👤 ")
                        +name972+" • "+role972,14,DARK,true);
                    memberList951.addView(card972,new LinearLayout.LayoutParams(-1,dp(44)));
                    if(owner.equals(me.getUid())&&!uid972.equals(me.getUid()))
                        card972.setOnLongClickListener(v->{
                            new AlertDialog.Builder(this).setTitle(name972)
                                .setMessage("Remove this account from Family?")
                                .setPositiveButton("Remove",(dialog,which)->
                                    r.collection("members").document(uid972).delete()
                                        .addOnFailureListener(error->
                                            toast("Could not remove Family member")))
                                .setNegativeButton("Cancel",null).show();
                            return true;
                        });
                }
                filterFamilyMembers956(memberList951,
                    memberSearch956.getText()==null?"":memberSearch956.getText().toString());
            });

''' + s[b:]
print('PASS Family member list is realtime and member removal instantly reflected on all phones')

# Family chat is already a realtime listener. Serialize Send and distinguish
# errors instead of creating multiple messages on double tap.
start='TextView send=button("Send",()->{String t=msg.getText().toString().trim();'
end='LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(72),dp(48));'
a=s.find(start);b=s.find(end,a)
if a<0 or b<0 or s.count(start)!=1:raise SystemExit('Family chat inline Send method missing')
s=s[:a]+r'''final boolean[] familySending972={false};
            TextView send=button("Send",()->{
                String t=msg.getText().toString().trim();
                if(!KingFamilyChat972.validMessage(t)){
                    toast("Enter a Family message of up to 1000 characters");return;
                }
                if(familySending972[0]){toast("Family message sending…");return;}
                familySending972[0]=true;
                Map<String,Object> message972=new HashMap<>();
                message972.put("uid",me.getUid());
                message972.put("name",displayName);
                message972.put("text",t);
                message972.put("createdAt",FieldValue.serverTimestamp());
                r.collection("messages").add(message972)
                    .addOnSuccessListener(v->{
                        familySending972[0]=false;
                        if(!isFinishing()&&!isDestroyed()
                           &&msg.getText().toString().trim().equals(t))msg.setText("");
                    })
                    .addOnFailureListener(error->{
                        familySending972[0]=false;
                        if(!isFinishing()&&!isDestroyed())
                            toast("Family message NOT sent • check network and Family membership");
                    });
            });
            '''+s[b:]
print('PASS Family Chat send serializes requests and reports Firebase delivery failures')
f.write_text(s)

# ----------------------------------------------------------------------
# Inbox: eliminate name-only fake chats when a real Firebase person is wanted.
# Resolve six-digit alias to canonical public profile UID, with collision choice.
# ----------------------------------------------------------------------
f=pkg/'InboxActivity.java';s=f.read_text()
s=replace_java_method(s,'    private void newChatDialog() {',
r'''    private void newChatDialog(){
        if(me==null||db==null){
            Toast.makeText(this,"Sign in with Google to message real KING Plus people",Toast.LENGTH_LONG).show();
            return;
        }
        final EditText userId972=new EditText(this);
        userId972.setHint("6-digit KING ID or verified Firebase UID");
        userId972.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("New real KING Plus Chat")
            .setMessage("Enter your friend's KING ID or exact verified account UID.")
            .setView(userId972).setNegativeButton("Cancel",null)
            .setPositiveButton("Find",(dialog,which)->{
                String value972=userId972.getText().toString().trim();
                if(KingIdentity964.sixDigits(value972)){
                    db.collection("public_profiles").whereEqualTo("publicId",value972)
                        .limit(15).get().addOnSuccessListener(profiles972->{
                            if(isFinishing()||isDestroyed())return;
                            java.util.ArrayList<DocumentSnapshot> options972=new java.util.ArrayList<>();
                            for(DocumentSnapshot p972:profiles972.getDocuments()){
                                if(!me.getUid().equals(p972.getId()))options972.add(p972);
                            }
                            if(options972.isEmpty()){
                                Toast.makeText(this,"No verified user found with that KING ID",Toast.LENGTH_LONG).show();return;
                            }
                            if(options972.size()==1){openVerifiedChat972(options972.get(0));return;}
                            String[] choices972=new String[options972.size()];
                            for(int i=0;i<options972.size();i++){
                                DocumentSnapshot profile972=options972.get(i);
                                String name972=profile972.getString("displayName");
                                if(name972==null||name972.trim().isEmpty())name972="KING Member";
                                choices972[i]=name972+" • "+profile972.getId().substring(
                                    0,Math.min(8,profile972.getId().length()));
                            }
                            new AlertDialog.Builder(this).setTitle("Choose the correct KING member")
                                .setItems(choices972,(d,index)->
                                    openVerifiedChat972(options972.get(index)))
                                .setNegativeButton("Cancel",null).show();
                        }).addOnFailureListener(error->
                            Toast.makeText(this,"User lookup failed: "+error.getLocalizedMessage(),
                                Toast.LENGTH_LONG).show());
                }else if(KingIdentity964.firebaseUid(value972)){
                    db.collection("public_profiles").document(value972).get()
                        .addOnSuccessListener(profile972->{
                            if(!isFinishing()&&!isDestroyed()){
                                if(profile972.exists())openVerifiedChat972(profile972);
                                else Toast.makeText(this,"Verified user not found",Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(error->
                            Toast.makeText(this,"UID lookup failed",Toast.LENGTH_SHORT).show());
                }else Toast.makeText(this,
                    "Enter a valid 6-digit KING ID or full Firebase UID",Toast.LENGTH_LONG).show();
            }).show();
    }
    private void openVerifiedChat972(DocumentSnapshot profile972){
        if(profile972==null||!profile972.exists()||me==null)return;
        String name972=profile972.getString("displayName");
        if(name972==null||name972.trim().isEmpty())name972="KING Member";
        openChat(name972,profile972.getId());
    }''','New Chat resolves real Firebase UID instead of allowing name-only fake messaging')
s=replace_java_method(s,'    private void openChat(String name,String peerUid){',
r'''    private void openChat(String name,String peerUid){
        if(me==null||!KingFamilyChat972.validPeer(me.getUid(),peerUid)){
            Toast.makeText(this,"Choose a different verified KING Plus account to open real Chat",
                Toast.LENGTH_LONG).show();
            return;
        }
        Intent i=new Intent(this,ChatActivity.class);
        i.putExtra("peerName",name);
        i.putExtra("peerUid",peerUid);
        startActivity(i);
    }''','real Chat always opens for a different verified Firebase UID')
f.write_text(s)

# ----------------------------------------------------------------------
# VIP: server wallet points (if backend publishes them) are the authority;
# otherwise VIP is clearly a local preview, not a paid entitlement.
# ----------------------------------------------------------------------
f=pkg/'KingVipVisualActivity.java';s=f.read_text()
s=exact(s,
 'public class KingVipVisualActivity extends Activity {',
 '''public class KingVipVisualActivity extends Activity {
    private Long verifiedVipPoints972;
    private com.google.firebase.firestore.ListenerRegistration vipWalletListener972;''',
 'track authenticated backend VIP balance')
s=exact(s,
 'public void onCreate(Bundle b){super.onCreate(b);build();}',
 '''public void onCreate(Bundle b){super.onCreate(b);build();connectVerifiedVip972();}''',
 'read server-controlled VIP wallet instead of relying only on device prefs')
s=exact(s,
 'LevelSystem.Snapshot s=LevelSystem.read(this);int vip=s.vipLevel;',
 '''LevelSystem.Snapshot s=LevelSystem.read(this);
        final long vipProgress972=verifiedVipPoints972==null
            ?s.vipPoints:verifiedVipPoints972.longValue();
        int vip=verifiedVipPoints972==null?s.vipLevel:LevelSystem.vipFor(vipProgress972);''',
 'VIP screen computes level from server points where verified')
s=exact(s,
 'long from=LevelSystem.vipThreshold(vip),to=s.nextVipPoints();int pc=vip>=LevelSystem.MAX_VIP?100:(int)Math.max(0,Math.min(100,(s.vipPoints-from)*100/Math.max(1,to-from)));',
 'long from=LevelSystem.vipThreshold(vip),to=LevelSystem.vipThreshold(vip+1);int pc=vip>=LevelSystem.MAX_VIP?100:(int)Math.max(0,Math.min(100,(vipProgress972-from)*100/Math.max(1,to-from)));',
 'VIP progress bar uses matching backend points and level')
s=exact(s,
 'TextView note=tv("No billing enabled • VIP progression uses KING Plus activity/test points",11,0xff9da6b6,false);',
 '''TextView note=tv(verifiedVipPoints972==null
            ?"LOCAL VIP PREVIEW ONLY • server-verified VIP is unavailable. TEST points are not paid entitlements."
            :"VERIFIED VIP • "+vipProgress972+" server-earned points. Progress is synchronized from your secure wallet.",
            11,0xff9da6b6,false);''',
 'differentiate cloud-verified VIP from TEST progress')
# Introduce new methods after build() by injecting before one of its unique
# class methods to avoid ending up outside class.
anchor='    private void build(){'
addition=r'''    private void connectVerifiedVip972(){
        com.google.firebase.auth.FirebaseUser account972=
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if(account972==null)return;
        String requestedUid972=account972.getUid();
        vipWalletListener972=com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("wallets").document(requestedUid972)
            .addSnapshotListener((doc972,error972)->{
                if(isFinishing()||isDestroyed())return;
                com.google.firebase.auth.FirebaseUser now972=
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                if(now972==null||!requestedUid972.equals(now972.getUid()))return;
                Long points972=error972==null&&doc972!=null&&doc972.exists()
                    ?doc972.getLong("vipPoints"):null;
                if(points972!=null)points972=Math.max(0,points972);
                if(java.util.Objects.equals(verifiedVipPoints972,points972))return;
                verifiedVipPoints972=points972;
                build();
            });
    }
    @Override protected void onDestroy(){
        if(vipWalletListener972!=null){vipWalletListener972.remove();vipWalletListener972=null;}
        super.onDestroy();
    }

'''
s=exact(s,anchor,addition+anchor,'safely monitor secure VIP wallet and unsubscribe on Activity destruction')
f.write_text(s)

gradle=root/'app/build.gradle';g=gradle.read_text()
old="versionCode 162; versionName '9.7.1-secure-live-gifts-emoji'"
if g.count(old)!=1:raise SystemExit('Expected successful v9.7.1 Android source')
gradle.write_text(g.replace(old,"versionCode 163; versionName '9.7.2-vip-family-realtime-chat'",1))
print("PASS KING Plus v9.7.2 – Gifts, VIP, Family, Chat and authenticated UID")
