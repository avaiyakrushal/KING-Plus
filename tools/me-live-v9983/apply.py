#!/usr/bin/env python3
"""KING v9.9.83 real Me Gift Wall, Gift History, verified Wallet Ledger,
cloud-synced per-user Gift Wish, visible on public profiles.

Apply to FULL signed v9.9.82 source archive, NOT old main.
"""
from pathlib import Path
import re,shutil,sys
root=Path(sys.argv[1])
java=root/'app/src/main/java/com/kingplus/social'
here=Path(__file__).resolve().parent
shutil.copyfile(here/'KingMeGiftSummary9983.java',java/'KingMeGiftSummary9983.java')

p=java/'KingDeepFlowActivity.java'
s=p.read_text()
def method(start,end,new):
 global s
 a=s.find(start);b=s.find(end,a+len(start))
 assert a>=0 and b>a, 'Expected method anchor missing '+start
 s=s[:a]+new.rstrip()+'\n\n'+s[b:]

method('    private void walletHistory(){','    private final String[] libraryFamilies9934=', '''
    private void walletHistory(){
        hero("🧾 Verified Wallet Ledger","Real Firebase transactions only. No phone-local TEST balance or imaginary paid credit.");
        fetchAccountLedger9983(false,false);
    }
''')

method('    private void giftWall(){','    private void gameCenter(){', '''
    private void giftWall(){
        if(!roomId.isEmpty()){
            hero("🧱 Room Gift Wall","Real free gift events from this Party Room only.");
            if(db==null){chip("Firebase unavailable",()->{});return;}
            db.collection("live_rooms").document(roomId).collection("events")
                .orderBy("createdAt",com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(100).get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(q->{
                    if(isFinishing()||isDestroyed()||!"gift_wall".equals(route))return;
                    Map<String,Long> counts=new HashMap<>();
                    for(DocumentSnapshot d:q.getDocuments())if("gift".equals(d.getString("type"))){
                        String target=d.getString("targetUid"),gift=d.getString("giftName");
                        if(target==null||gift==null||gift.trim().isEmpty())continue;
                        counts.put(gift,counts.getOrDefault(gift,0L)+1);
                    }
                    if(counts.isEmpty())chip("No recorded room gifts yet",()->{});
                    else for(Map.Entry<String,Long> g:counts.entrySet())
                        chip("🎁 "+g.getKey()+" ×"+g.getValue(),()->{});
                }).addOnFailureListener(e->{
                    if(!isFinishing()&&"gift_wall".equals(route))
                        chip("Room gift events unavailable online",()->{});
                });
            return;
        }
        hero("🧱 My Gift Wall","Gifts received by your signed-in account in the trusted Wallet Ledger. Free Party gifts are visible in the Party Room.");
        fetchAccountLedger9983(true,true);
    }

    private void giftHistory(){
        if(!roomId.isEmpty()){
            hero("🕘 Room Gift History","Real gifts in this live room, not the whole account.");
            if(db==null)return;
            db.collection("live_rooms").document(roomId).collection("events")
                .orderBy("createdAt",com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(80).get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(q->{
                    if(isFinishing()||isDestroyed()||!"gift_history".equals(route))return;
                    int shown=0;
                    for(DocumentSnapshot d:q.getDocuments())if("gift".equals(d.getString("type"))){
                        chip("🎁 "+safe(d.getString("text"),"Recorded gift"),()->{});shown++;
                    }
                    if(shown==0)chip("No room gifts recorded",()->{});
                }).addOnFailureListener(e->{
                    if(!isFinishing()&&"gift_history".equals(route))
                        chip("Room gift history unavailable online",()->{});
                });
            return;
        }
        hero("🕘 My Gift History","Server-recorded paid gifts sent or received by your account only. Free Party gift receipts are in the original rooms.");
        fetchAccountLedger9983(true,false);
    }

    /** Never display local pretend wallet transactions as verified payments. */
    private void fetchAccountLedger9983(boolean giftsOnly,boolean receivedOnly){
        if(db==null||me==null){chip("Log in to view your actual account ledger",()->{});return;}
        final String owner=me.getUid(),expectedRoute=route;
        final java.util.ArrayList<DocumentSnapshot> records=new java.util.ArrayList<>();
        final int[] pending={receivedOnly?1:2};
        final boolean[] failed={false};
        chip("Loading real Firebase ledger…",()->{});
        java.util.function.Consumer<com.google.firebase.firestore.QuerySnapshot> onPage=q->{
            if(q!=null)records.addAll(q.getDocuments());
            pending[0]--;
            if(pending[0]!=0)return;
            if(isFinishing()||isDestroyed()||!expectedRoute.equals(route)||me==null||
               !owner.equals(me.getUid()))return;
            if(failed[0])chip("Some transactions could not be retrieved. History may be incomplete.",()->{});
            java.util.HashMap<String,DocumentSnapshot> unique=new java.util.HashMap<>();
            for(DocumentSnapshot d:records) {
                if(owner.equals(d.getString("senderUid"))||owner.equals(d.getString("targetUid")))
                    unique.put(d.getId(),d);
            }
            java.util.ArrayList<DocumentSnapshot> ordered=new java.util.ArrayList<>(unique.values());
            ordered.sort((a,b)->{
                com.google.firebase.Timestamp at=a.getTimestamp("createdAt");
                com.google.firebase.Timestamp bt=b.getTimestamp("createdAt");
                long x=at==null?0:at.toDate().getTime(),y=bt==null?0:bt.toDate().getTime();
                return Long.compare(y,x);
            });
            java.util.ArrayList<KingMeGiftSummary9983.Gift> gifts=new java.util.ArrayList<>();
            for(DocumentSnapshot d:ordered) {
                com.google.firebase.Timestamp at=d.getTimestamp("createdAt");
                Long cost=d.getLong("cost");
                String name=d.getString("giftName");
                gifts.add(new KingMeGiftSummary9983.Gift(d.getId(),
                    d.getString("senderUid"),d.getString("targetUid"),
                    name,cost==null?0:cost,at==null?0:at.toDate().getTime()));
            }
            java.util.List<KingMeGiftSummary9983.Gift> verified=
                KingMeGiftSummary9983.forAccount(gifts,owner,receivedOnly);
            if(receivedOnly){
                Map<String,Integer> counts=KingMeGiftSummary9983.receivedWall(gifts,owner);
                if(counts.isEmpty())chip("No verified received gifts in the Wallet Ledger yet.",()->{});
                else for(Map.Entry<String,Integer> d:counts.entrySet())
                    chip("🎁 "+d.getKey()+" ×"+d.getValue(),()->{});
            }else if(giftsOnly){
                if(verified.isEmpty())chip("No verified paid gifts recorded for your account.",()->{});
                for(KingMeGiftSummary9983.Gift d:verified){
                    boolean sent=owner.equals(d.senderUid);
                    chip((sent?"↗ Sent ":"↙ Received ")+d.name+" • "+d.amount+" 💎",()->{});
                }
            }else{
                int shown=0;
                for(DocumentSnapshot d:ordered) {
                    if(shown++>=60)break;
                    boolean sent=owner.equals(d.getString("senderUid"));
                    String name=safe(d.getString("giftName"),
                        safe(d.getString("type"),"Verified transaction"));
                    Long amount=d.getLong("cost");
                    if(amount==null)amount=d.getLong("amount");
                    chip((sent?"↗ ":"↙ ")+name+" • "+(amount==null?"—":amount)+" 💎",()->{});
                }
                if(shown==0)chip("No server-verified wallet transactions yet.",()->{});
            }
        };
        db.collection("wallet_ledger").whereEqualTo("targetUid",owner).limit(60)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(onPage::accept)
            .addOnFailureListener(e->{failed[0]=true;onPage.accept(null);});
        if(!receivedOnly)db.collection("wallet_ledger").whereEqualTo("senderUid",owner).limit(60)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(onPage::accept)
            .addOnFailureListener(e->{failed[0]=true;onPage.accept(null);});
    }

    private void giftWish(){
        hero("💫 My Live Gift Wish","This gift wish is saved to your KING Plus Firebase account and visible on your public profile.");
        if(db==null||me==null){chip("Sign in to choose a wish",()->{});return;}
        final String signedUid=me.getUid();
        final TextView current=tv("Loading current Wish from Firebase…",13,sub(),true);
        body.addView(current,new LinearLayout.LayoutParams(-1,dp(50)));
        db.collection("king_me_wishes").document(signedUid)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(d->{
                if(isFinishing()||isDestroyed()||!"gift_wish".equals(route)||me==null||
                    !signedUid.equals(me.getUid()))return;
                String wish=d.exists()?d.getString("wish"):null;
                current.setText(wish==null?"No saved Gift Wish yet":"🎁 Current wish: "+wish);
            }).addOnFailureListener(e->current.setText("Gift Wish unavailable online. Nothing changed."));
        final String[] options={"Royal Castle","Galaxy Ship","King Dragon","VIP Crown","Diamond Rain"};
        for(String option:options)row("🎁",option,"Save as my public Gift Wish",()->{
            if(!KingMeGiftSummary9983.validWish(option)||me==null||
               !signedUid.equals(me.getUid())){toast("Sign in again");return;}
            Map<String,Object> data=new HashMap<>();
            data.put("uid",signedUid);data.put("wish",option);
            data.put("updatedAt",FieldValue.serverTimestamp());
            db.collection("king_me_wishes").document(signedUid).set(data)
                .addOnSuccessListener(v->{
                    if(!isFinishing()&&!isDestroyed()&&"gift_wish".equals(route))
                        current.setText("🎁 Current wish: "+option+"  ✓ Synced online");
                })
                .addOnFailureListener(e->toast("Gift Wish not saved online: "+safe(e.getMessage(),"Permission")));
        });
        chip("Wish is a profile preference only. It gives no free Diamonds or paid Gifts.",()->{});
    }
''')


# Retain Lucky Gift submenu, but do NOT advertise imaginary prizes.
assert s.count('    private void gameCenter(){') == 1
s=s.replace('    private void gameCenter(){', '''    private void giftLucky(){
        hero("🍀 Lucky Gift","Real random reward settlement is not enabled in this TEST build.");
        chip("No false Jackpot, Diamonds or VIP gifts are awarded locally.",()->{});
        row("🎁","Gift Center","Open available verified free gift catalog",this::openGiftCenter);
    }

    private void gameCenter(){''',1)

p.write_text(s)
profile=java/'KingPublicProfileActivity.java'
q=profile.read_text()
anchor='''        TextView cosmetics=tv("🖼 "+frame'''
assert q.count(anchor)==1, 'expected public profile cosmetics section'
new='''        // Real public Gift Wish: resolves from the saved KING account, not
        // private phone preferences or invented demo gift achievements.
        final String wishUid9983=uid;
        TextView wishBadge9983=tv("🎁 Gift Wish • Loading…",13,PURPLE,true);
        wishBadge9983.setBackground(bg(0xfffff6d5,12));
        LinearLayout.LayoutParams wishLp9983=new LinearLayout.LayoutParams(-1,dp(42));
        wishLp9983.topMargin=dp(4);
        body.addView(wishBadge9983,wishLp9983);
        db.collection("king_me_wishes").document(wishUid9983)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(w->{
                if(isFinishing()||isDestroyed()||!wishUid9983.equals(uid))return;
                String chosen=w.exists()?w.getString("wish"):null;
                wishBadge9983.setText(KingMeGiftSummary9983.validWish(chosen)
                    ?"🎁 Gift Wish • "+chosen:"🎁 No Gift Wish selected");
            }).addOnFailureListener(err->{
                if(!isFinishing()&&!isDestroyed()&&wishUid9983.equals(uid))
                    wishBadge9983.setText("🎁 Gift Wish unavailable online");
            });
'''
q=q.replace(anchor,new+anchor,1)
profile.write_text(q)

rules=root/'firestore.rules'
r=rules.read_text()
anchor='''    match /wallets/{uid} {'''
assert r.count(anchor)==1
newrules='''    // Public gift WISH is a profile preference, NOT wallet credit or paid
    // entitlement. Only the account owner can create/change a fixed wish.
    match /king_me_wishes/{uid} {
      allow read: if signedIn();
      allow create: if activeUser() && owner(uid)
        && request.resource.data.keys().hasOnly(['uid','wish','updatedAt'])
        && request.resource.data.keys().hasAll(['uid','wish','updatedAt'])
        && request.resource.data.uid == uid
        && request.resource.data.wish in [
          'Royal Castle','Galaxy Ship','King Dragon','VIP Crown','Diamond Rain']
        && request.resource.data.updatedAt == request.time;
      allow update: if activeUser() && owner(uid)
        && resource.data.uid == uid
        && request.resource.data.keys().hasOnly(['uid','wish','updatedAt'])
        && request.resource.data.keys().hasAll(['uid','wish','updatedAt'])
        && request.resource.data.uid == resource.data.uid
        && request.resource.data.wish in [
          'Royal Castle','Galaxy Ship','King Dragon','VIP Crown','Diamond Rain']
        && request.resource.data.updatedAt == request.time;
      allow delete: if owner(uid);
    }

'''
r=r.replace(anchor,newrules+anchor,1)
rules.write_text(r)
gradle=root/'app/build.gradle'
g=gradle.read_text()
g,n=re.subn(r"versionCode\s+261\s*;\s*versionName\s+'[^']+'",
  "versionCode 262; versionName '9.9.83-me-live-gifts-wishes-test'",g)
assert n==1,'Must be signed 9.9.82 source with versionCode261'
gradle.write_text(g)
assert 'KingAgency9982Activity' in (root/'app/src/main/AndroidManifest.xml').read_text()
assert 'LIVE_RECHARGE = false' in (java/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (java/'KingPremiumAccess981.java').read_text()
assert 'KingLiveBingo9980Activity' in (root/'app/src/main/AndroidManifest.xml').read_text()
print("PASS server-verified Me Gift Wall & Wallet History, synced Wish public profile and versionCode262")
