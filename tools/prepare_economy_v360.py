from pathlib import Path
import re

PARTY = Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
MAIN = Path('app/src/main/java/com/kingplus/social/MainActivity.java')
RULES = Path('firestore.rules')
FUNCTIONS = Path('functions/v3.js')
BUILD = Path('app/build.gradle')


def replace_once(text, old, new, label):
    if old not in text:
        raise SystemExit('v3.6.0 template changed: ' + label)
    return text.replace(old, new, 1)

# Party: use the shared TEST economy balance and expose Economy Center.
src = PARTY.read_text(encoding='utf-8')
src = replace_once(
    src,
    '        localCoins = prefs.getInt("coins", 2500);\n',
    '        localCoins = EconomyManager.getTestCoins(this,prefs.getInt("coins",2500));\n',
    'Party balance init')

src = replace_once(
    src,
    '        items.add("ℹ Room info"); items.add("👤 Host profile");\n',
    '        items.add("ℹ Room info"); items.add("👤 Host profile"); items.add("💎 Economy center");\n',
    'Party menu economy item')

src = replace_once(
    src,
    '            else if(x.contains("Host profile"))hostProfileDialog();\n',
    '            else if(x.contains("Host profile"))hostProfileDialog();\n            else if(x.contains("Economy center"))startActivity(new Intent(this,EconomyActivity.class));\n',
    'Party menu economy dispatch')

# Replace gift send methods after v3.2/v3.4 transformations.
pattern = re.compile(r'    private void giftDialogFor\(String targetUid,String targetLabel\) \{.*?^    private void shareRoom\(\)\{', re.S | re.M)
replacement = r'''    private void giftDialogFor(String targetUid,String targetLabel) {
        localCoins=EconomyManager.getTestCoins(this,localCoins);
        String[] gifts={"🌹 Rose • 10","❤️ Heart • 50","🍫 Chocolate • 100","🚗 Car • 500","👑 Crown • 1000","🏰 Castle • 5000","🎆 Firework • 10000"};
        int[] costs={10,50,100,500,1000,5000,10000};String[] names={"Rose","Heart","Chocolate","Car","Crown","Castle","Firework"};
        String levels="W"+EconomyManager.wealthLevel(this)+" • C"+EconomyManager.charmLevel(this);
        new AlertDialog.Builder(this).setTitle("🎁 "+targetLabel+" • "+localCoins+" TEST coins • "+levels).setItems(gifts,(d,w)->sendGiftTo(targetUid,targetLabel,names[w],costs[w])).setNegativeButton("Close",null).show();
    }
    private void sendGift(String gift,int cost) { sendGiftTo(ownerUid==null?"":ownerUid,ownerName,gift,cost); }
    private void sendGiftTo(String targetUid,String targetName,String gift,int cost) {
        if(cloudRoom&&user!=null&&targetUid!=null&&!targetUid.isEmpty()&&!targetUid.equals(user.getUid())){
            CloudBackend.sendGift(targetUid,gift,cost,roomId,(ok,message)->runOnUiThread(()->{
                toast(message);
                if(ok){
                    EconomyManager.noteCloudGiftSent(this,cost,gift,targetName);
                    addGiftEventV360(targetUid,targetName,gift,cost);
                }
            }));return;
        }
        if(!EconomyManager.spendTestCoins(this,cost,gift,targetName)){toast("Not enough TEST coins");return;}
        localCoins=EconomyManager.getTestCoins(this,localCoins);prefs.edit().putInt("coins",localCoins).apply();
        addGiftEventV360(targetUid,targetName,gift,cost);
        toast("Gift sent • TEST balance "+localCoins+" • Wealth Lv."+EconomyManager.wealthLevel(this));
    }
    private void addGiftEventV360(String targetUid,String targetName,String gift,int cost){
        String text=safeName()+" sent "+gift+" to "+targetName+" 🎁 x1 • "+cost+" coins";
        if(cloudRoom&&user!=null&&db!=null&&roomId!=null){
            Map<String,Object>d=new HashMap<>();d.put("actorUid",user.getUid());d.put("actorName",safeName());d.put("type","gift");d.put("text",text);d.put("giftName",gift);d.put("giftValue",cost);d.put("targetUid",targetUid==null?"":targetUid);d.put("targetName",targetName==null?"User":targetName);d.put("createdAt",FieldValue.serverTimestamp());
            db.collection("live_rooms").document(roomId).collection("events").add(d);
        } else addFeed(text);
    }
    private void shareRoom(){'''
src, count = pattern.subn(replacement, src, count=1)
if count != 1:
    raise SystemExit('v3.6.0 gift flow template changed')

# Ranking now sums gift value, not only gift count.
pattern = re.compile(r'    private void roomRankingDialog\(\)\{.*?^    private void allSeatRequestsDialog\(\)\{', re.S | re.M)
replacement = r'''    private void roomRankingDialog(){
        if(!cloudRoom||db==null){new AlertDialog.Builder(this).setTitle("🏆 Room ranking").setMessage("TEST room gifts increase Wealth locally. Live room ranking uses secure gift values.").setPositiveButton("OK",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(150).get()
            .addOnSuccessListener(snap->{
                Map<String,Long>score=new HashMap<>();
                for(DocumentSnapshot d:snap.getDocuments())if("gift".equals(d.getString("type"))){String n=str(d,"actorName","User");Long raw=d.getLong("giftValue");long value=raw==null?1L:Math.max(1L,raw);score.put(n,score.containsKey(n)?score.get(n)+value:value);}
                List<Map.Entry<String,Long>>list=new ArrayList<>(score.entrySet());java.util.Collections.sort(list,(a,b)->Long.compare(b.getValue(),a.getValue()));
                List<String>rows=new ArrayList<>();int rank=1;for(Map.Entry<String,Long>e:list){rows.add((rank==1?"🥇 ":rank==2?"🥈 ":rank==3?"🥉 ":rank+". ")+e.getKey()+" • "+e.getValue()+" gift coins");rank++;if(rank>20)break;}
                showRows("🏆 Room gift ranking",rows,"No gift ranking yet");
            }).addOnFailureListener(e->toast(msg(e)));
    }
    private void allSeatRequestsDialog(){'''
src, count = pattern.subn(replacement, src, count=1)
if count != 1:
    raise SystemExit('v3.6.0 room ranking template changed')
PARTY.write_text(src, encoding='utf-8')

# Main profile: wire the v3.1.2 Me/Profile wallet card to Economy Center and show progression.
main = MAIN.read_text(encoding='utf-8')
main = replace_once(
    main,
    'walletTitle.setText("My Wallet");',
    'walletTitle.setText("Economy Center");',
    'profile wallet title')
main = replace_once(
    main,
    'TextView coins=smallBadge("💎 "+coinBalance,0xff4b326c,Color.WHITE);',
    'TextView coins=smallBadge("💎 "+EconomyManager.getTestCoins(this,coinBalance),0xff4b326c,Color.WHITE);',
    'profile wallet balance')
main = replace_once(
    main,
    'TextView gifts=smallBadge("🎁 "+giftCount,0xff4b326c,Color.WHITE);',
    'TextView gifts=smallBadge("🎁 "+EconomyManager.giftsSent(this),0xff4b326c,Color.WHITE);',
    'profile gift count')
main = replace_once(
    main,
    'wallet.setOnClickListener(v->walletPage());',
    'wallet.setOnClickListener(v->startActivity(new Intent(this,EconomyActivity.class)));',
    'profile economy click')
main = replace_once(
    main,
    'TextView lv=smallBadge("Lv."+level,0xff8755e8,Color.WHITE);',
    'TextView lv=smallBadge("Lv."+EconomyManager.userLevel(this),0xff8755e8,Color.WHITE);',
    'profile user level')
main = replace_once(
    main,
    'id.setText("ID: "+uid+"   ⧉");',
    'id.setText("ID: "+uid+"   ⧉   W"+EconomyManager.wealthLevel(this)+" C"+EconomyManager.charmLevel(this)+" • "+EconomyManager.equippedProfileFrame(this));',
    'profile progression label')
main = replace_once(
    main,
    'else if(w==9)walletPage();',
    'else if(w==9)startActivity(new Intent(this,EconomyActivity.class));',
    'side menu economy')
MAIN.write_text(main, encoding='utf-8')

# Firestore: progression is server-owned/readable; clients cannot forge levels or gift totals.
rules = RULES.read_text(encoding='utf-8')
marker = '    match /test_wallet_snapshots/{uid} {'
economy_rules = '''    match /economy_profiles/{uid} {
      allow read: if signedIn();
      allow write: if false;
    }

'''
if 'match /economy_profiles/{uid}' not in rules:
    if marker not in rules:
        raise SystemExit('v3.6.0 economy rules marker missing')
    rules = rules.replace(marker, economy_rules + marker, 1)
RULES.write_text(rules, encoding='utf-8')

# Server gift transaction: update sender Wealth and receiver Charm in the same idempotent transaction.
fn = FUNCTIONS.read_text(encoding='utf-8')
fn = replace_once(
    fn,
    "  const giftName = cleanText(request.data && request.data.giftName, 60) || 'Gift';\n  const cost = Number(request.data && request.data.cost);\n",
    "  const giftName = cleanText(request.data && request.data.giftName, 60) || 'Gift';\n  const roomId = cleanText(request.data && request.data.roomId, 160);\n  const cost = Number(request.data && request.data.cost);\n",
    'function room id')
fn = replace_once(
    fn,
    "  const receiverRef = db.collection('wallets').doc(targetUid);\n  const ledgerRef = db.collection('wallet_ledger').doc(`gift_${hash(operationId)}`);\n",
    "  const receiverRef = db.collection('wallets').doc(targetUid);\n  const senderEconomyRef = db.collection('economy_profiles').doc(senderUid);\n  const receiverEconomyRef = db.collection('economy_profiles').doc(targetUid);\n  const ledgerRef = db.collection('wallet_ledger').doc(`gift_${hash(operationId)}`);\n",
    'economy refs')
fn = replace_once(
    fn,
    "    tx.set(receiverRef, { coins: receiverCoins + cost, updatedAt: FieldValue.serverTimestamp() }, { merge: true });\n    tx.set(ledgerRef, {\n      type: 'gift', senderUid, targetUid, giftName, cost,\n      requestIdHash: hash(requestId), createdAt: FieldValue.serverTimestamp()\n    });\n",
    "    tx.set(receiverRef, { coins: receiverCoins + cost, updatedAt: FieldValue.serverTimestamp() }, { merge: true });\n    tx.set(senderEconomyRef, { uid: senderUid, sentCoins: FieldValue.increment(cost), giftsSent: FieldValue.increment(1), wealthXp: FieldValue.increment(cost), userXp: FieldValue.increment(Math.max(1, Math.floor(cost / 10))), updatedAt: FieldValue.serverTimestamp() }, { merge: true });\n    tx.set(receiverEconomyRef, { uid: targetUid, receivedCoins: FieldValue.increment(cost), giftsReceived: FieldValue.increment(1), charmXp: FieldValue.increment(cost), userXp: FieldValue.increment(Math.max(1, Math.floor(cost / 10))), updatedAt: FieldValue.serverTimestamp() }, { merge: true });\n    tx.set(ledgerRef, {\n      type: 'gift', senderUid, targetUid, giftName, cost, roomId,\n      requestIdHash: hash(requestId), createdAt: FieldValue.serverTimestamp()\n    });\n",
    'economy transaction')
fn = replace_once(
    fn,
    "      type: 'gift', senderUid, targetUid, senderBalance,\n      requestIdHash: hash(requestId), createdAt: FieldValue.serverTimestamp()\n",
    "      type: 'gift', senderUid, targetUid, senderBalance, roomId,\n      requestIdHash: hash(requestId), createdAt: FieldValue.serverTimestamp()\n",
    'operation room context')
FUNCTIONS.write_text(fn, encoding='utf-8')

# Version bump after all earlier preparation stages.
gradle = BUILD.read_text(encoding='utf-8')
gradle = re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 52; versionName '3.6.0'", gradle)
BUILD.write_text(gradle, encoding='utf-8')
print('Prepared KING Plus v3.6.0 gifts, wallet, levels, VIP and frames')
