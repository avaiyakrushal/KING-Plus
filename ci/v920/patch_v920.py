from pathlib import Path
import sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
(pkg/'KingProductionTestActivity.java').write_text(Path(__file__).with_name('KingProductionTestActivity.java').read_text())

party=pkg/'PartyActivity.java'
s=party.read_text()

old='''String[] icons={"🎟","🪑","✋","💬","🎵","🎨","📋","👑","🏰","🛡","📊","🎤","📻","🎁","💫","🔎","📢","🎙","📹","🎁","🏆","🗨","🎒","🌐","💞","⚔️","🔁","🎮","📦","✨","🎤","⚔️","🎲","🏰","💎","🚀"};'''
new='''String[] icons={"🎟","🪑","✋","💬","🎵","🎨","📋","👑","🏰","🛡","📊","🎤","📻","🎁","💫","🔎","📢","🎙","📹","🎁","🏆","🗨","🎒","🌐","💞","⚔️","🔁","🎮","📦","✨","🎤","⚔️","🎲","🏰","💎","🚀","🧪"};'''
if old not in s: raise SystemExit('tool icon array missing')
s=s.replace(old,new,1)

old='''String[] labels={"Events","Room seat",queueOn?"Queue ON":"Enable queue","Private chat","Music","Atmosphere","Income","Share Family","Theme Room","Party Master","Party Data","KTV Queue","Radio Mic","Lucky Gift","Gift Wish","Find User","Notice","Voice Room","Multi Video","Gift Wall","Gift Rank","Chat History","Backpack","Community","Relationship","Audio PK","Loop Mic","Room Games","Asset Pack","Parity Center","KTV Stage","PK Arena","Match","Party Stage","VIP Rank","Ecosystem Pro"};'''
new='''String[] labels={"Events","Room seat",queueOn?"Queue ON":"Enable queue","Private chat","Music","Atmosphere","Income","Share Family","Theme Room","Party Master","Party Data","KTV Queue","Radio Mic","Lucky Gift","Gift Wish","Find User","Notice","Voice Room","Multi Video","Gift Wall","Gift Rank","Chat History","Backpack","Community","Relationship","Audio PK","Loop Mic","Room Games","Asset Pack","Parity Center","KTV Stage","PK Arena","Match","Party Stage","VIP Rank","Ecosystem Pro","Production Test"};'''
if old not in s: raise SystemExit('tool labels missing')
s=s.replace(old,new,1)

old='''        else if("Ecosystem Pro".equals(label))openEcosystem880("home");'''
new='''        else if("Ecosystem Pro".equals(label))openEcosystem880("home");
        else if("Production Test".equals(label))openProductionTest920();'''
if old not in s: raise SystemExit('tool handler missing')
s=s.replace(old,new,1)

old='''        items.add("🧪 Online diagnostics"); items.add("🔗 Share room"); items.add("🔔 Invite by Firebase UID"); items.add("⚑ Report host"); items.add("🚫 Block host");'''
new='''        items.add("🧪 Final production test"); items.add("🧪 Online diagnostics"); items.add("🔗 Share room"); items.add("🔔 Invite by Firebase UID"); items.add("⚑ Report host"); items.add("🚫 Block host");'''
if old not in s: raise SystemExit('more menu insertion missing')
s=s.replace(old,new,1)

old='''        else if(x.contains("Online diagnostics"))onlineDiagnostics900();'''
new='''        else if(x.contains("Final production test"))openProductionTest920();
        else if(x.contains("Online diagnostics"))onlineDiagnostics900();'''
if old not in s: raise SystemExit('more menu handler missing')
s=s.replace(old,new,1)

old='''    private void openEcosystem880(String route){Intent i=new Intent(this,KingEcosystemProActivity.class);i.putExtra("route",route);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}'''
new='''    private void openEcosystem880(String route){Intent i=new Intent(this,KingEcosystemProActivity.class);i.putExtra("route",route);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}
    private void openProductionTest920(){if(roomId==null||!cloudRoom){toast("Open a live Firebase Party Room first");return;}Intent i=new Intent(this,KingProductionTestActivity.class);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}'''
if old not in s: raise SystemExit('open ecosystem helper missing')
s=s.replace(old,new,1)

old='''                String type=d.getString("type");
                if("gift".equals(type)){Long q=d.getLong("giftQty");Long v=d.getLong("giftValue");String gift=d.getString("giftName");showGiftEffect(str(d,"actorName","User"),str(d,"targetName","Host"),gift,giftIconFor(gift),q==null?1:q,v==null?1:v);}
'''
new='''                String type=d.getString("type");
                if("sync_test".equals(type)&&user!=null&&!user.getUid().equals(d.getString("actorUid"))){ackSyncTest920(room,d);}
                if("gift".equals(type)){Long q=d.getLong("giftQty");Long v=d.getLong("giftValue");String gift=d.getString("giftName");showGiftEffect(str(d,"actorName","User"),str(d,"targetName","Host"),gift,giftIconFor(gift),q==null?1:q,v==null?1:v);}
'''
if old not in s: raise SystemExit('event type marker missing')
s=s.replace(old,new,1)

marker='''    private void registerMember(){joinMemberThenOpen891();}
'''
helpers='''    private void ackSyncTest920(DocumentReference room,DocumentSnapshot ping){
        String testId=ping.getString("testId");if(testId==null||testId.trim().isEmpty()||user==null)return;
        String safeId=testId.replaceAll("[^A-Za-z0-9_-]","");if(safeId.length()>32)safeId=safeId.substring(0,32);
        DocumentReference ack=room.collection("events").document("ack_"+safeId+"_"+user.getUid());
        final String finalId=testId;
        ack.get().addOnSuccessListener(existing->{if(existing.exists())return;Map<String,Object>m=new HashMap<>();m.put("actorUid",user.getUid());m.put("actorName",safeName());m.put("type","sync_ack");m.put("testId",finalId);m.put("text",safeName()+" acknowledged production sync "+finalId);m.put("createdAt",FieldValue.serverTimestamp());ack.set(m).addOnFailureListener(e->KingStability.nonFatal(this,"sync-ack",e));}).addOnFailureListener(e->KingStability.nonFatal(this,"sync-ack-read",e));
    }

'''
if marker not in s: raise SystemExit('registerMember marker missing')
s=s.replace(marker,helpers+marker,1)
party.write_text(s)

manifest=root/'app/src/main/AndroidManifest.xml'
ms=manifest.read_text()
old='''        <activity android:name=".KingEcosystemProActivity" android:exported="false" />'''
new='''        <activity android:name=".KingEcosystemProActivity" android:exported="false" />
        <activity android:name=".KingProductionTestActivity" android:exported="false" />'''
if old not in ms: raise SystemExit('manifest marker missing')
manifest.write_text(ms.replace(old,new,1))

print('v9.2.0 final validation patch applied')
