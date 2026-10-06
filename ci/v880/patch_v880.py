from pathlib import Path
import sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
(pkg/'KingEcosystemProActivity.java').write_text(Path(__file__).with_name('KingEcosystemProActivity.java').read_text())
(pkg/'KingRoomStageView.java').write_text(Path(__file__).with_name('KingRoomStageView.java').read_text())

party=pkg/'PartyActivity.java'
s=party.read_text()
old='''        shell.setBackground(premiumRoomBackground());
        addRoomBackgroundGlow(shell);

        LinearLayout roomRoot = new LinearLayout(this);'''
new='''        shell.setBackground(premiumRoomBackground());
        addRoomBackgroundGlow(shell);
        KingRoomStageView stageBackdrop880=new KingRoomStageView(this,roomTheme);
        shell.addView(stageBackdrop880,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout roomRoot = new LinearLayout(this);'''
if old not in s: raise SystemExit('party stage insertion marker missing')
s=s.replace(old,new,1)

old='''String[] icons={"🎟","🪑","✋","💬","🎵","🎨","📋","👑","🏰","🛡","📊","🎤","📻","🎁","💫","🔎","📢","🎙","📹","🎁","🏆","🗨","🎒","🌐","💞","⚔️","🔁","🎮","📦","✨","🎤","⚔️","🎲","🏰","💎"};'''
new='''String[] icons={"🎟","🪑","✋","💬","🎵","🎨","📋","👑","🏰","🛡","📊","🎤","📻","🎁","💫","🔎","📢","🎙","📹","🎁","🏆","🗨","🎒","🌐","💞","⚔️","🔁","🎮","📦","✨","🎤","⚔️","🎲","🏰","💎","🚀"};'''
if old not in s: raise SystemExit('tool icons marker missing')
s=s.replace(old,new,1)
old='''String[] labels={"Events","Room seat",queueOn?"Queue ON":"Enable queue","Private chat","Music","Atmosphere","Income","Share Family","Theme Room","Party Master","Party Data","KTV Queue","Radio Mic","Lucky Gift","Gift Wish","Find User","Notice","Voice Room","Multi Video","Gift Wall","Gift Rank","Chat History","Backpack","Community","Relationship","Audio PK","Loop Mic","Room Games","Asset Pack","Parity Center","KTV Stage","PK Arena","Match","Party Stage","VIP Rank"};'''
new='''String[] labels={"Events","Room seat",queueOn?"Queue ON":"Enable queue","Private chat","Music","Atmosphere","Income","Share Family","Theme Room","Party Master","Party Data","KTV Queue","Radio Mic","Lucky Gift","Gift Wish","Find User","Notice","Voice Room","Multi Video","Gift Wall","Gift Rank","Chat History","Backpack","Community","Relationship","Audio PK","Loop Mic","Room Games","Asset Pack","Parity Center","KTV Stage","PK Arena","Match","Party Stage","VIP Rank","Ecosystem Pro"};'''
if old not in s: raise SystemExit('tool labels marker missing')
s=s.replace(old,new,1)
old='''        else if("VIP Rank".equals(label))openParity870("vip");'''
new='''        else if("VIP Rank".equals(label))openParity870("vip");
        else if("Ecosystem Pro".equals(label))openEcosystem880("home");'''
if old not in s: raise SystemExit('tool handler marker missing')
s=s.replace(old,new,1)

old='''        items.add("✨ KING Parity Center"); items.add("🎤 KTV Stage"); items.add("⚔️ PK Arena"); items.add("🎲 Match Center"); items.add("🏰 Party Stage"); items.add("💎 VIP & Rank");'''
new='''        items.add("✨ KING Parity Center"); items.add("🚀 Ecosystem Pro"); items.add("🎤 KTV Stage"); items.add("⚔️ PK Arena"); items.add("🎲 Match Center"); items.add("🏰 Party Stage"); items.add("💎 VIP & Rank");'''
if old not in s: raise SystemExit('room menu list marker missing')
s=s.replace(old,new,1)
old='''        else if(x.contains("Parity Center"))openParity870("home");'''
new='''        else if(x.contains("Ecosystem Pro"))openEcosystem880("home");
        else if(x.contains("Parity Center"))openParity870("home");'''
if old not in s: raise SystemExit('room menu handler marker missing')
s=s.replace(old,new,1)
old='''    private void openParity870(String route){Intent i=new Intent(this,KingParityHubActivity.class);i.putExtra("route",route);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}'''
new='''    private void openParity870(String route){Intent i=new Intent(this,KingParityHubActivity.class);i.putExtra("route",route);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}
    private void openEcosystem880(String route){Intent i=new Intent(this,KingEcosystemProActivity.class);i.putExtra("route",route);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}'''
if old not in s: raise SystemExit('open parity marker missing')
s=s.replace(old,new,1)
party.write_text(s)

hub=pkg/'KingParityHubActivity.java'
s=hub.read_text()
old='''private void home(){hero("✨ KING Plus Parity Center","Original KING Plus features inspired by modern social voice-room apps — no copied proprietary assets.");card("🎤","KTV Stage","Shared song requests and current singer stage",()->show("ktv"));'''
new='''private void home(){hero("✨ KING Plus Parity Center","Original KING Plus features inspired by modern social voice-room apps — no copied proprietary assets.");card("🚀","Ecosystem Pro","KTV recording, PK timer, Family treasury, Rank and Arcade",()->openPro880("home"));card("🎤","KTV Stage","Shared song requests and current singer stage",()->show("ktv"));'''
if old not in s: raise SystemExit('parity home marker missing')
s=s.replace(old,new,1)
insert='''    private void openFamily(){Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab","Family");startActivity(i);body.addView(tv("Family Center opened. Return here to continue.",13,MUTED,false));}
'''
replacement='''    private void openFamily(){openPro880("family");}
    private void openPro880(String route){Intent i=new Intent(this,KingEcosystemProActivity.class);i.putExtra("route",route);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}
'''
if insert not in s: raise SystemExit('parity family marker missing')
s=s.replace(insert,replacement,1)
hub.write_text(s)

gift=pkg/'KingGiftCenterActivity.java'
s=gift.read_text()
old='''    private final String[] names={"Rose","Heart","Crown","Star Castle","Royal Car","Galaxy","Golden Dragon","King Throne","Love Rain","Party Rocket","Diamond Crown","Universe"};
    private final String[] icons={"🌹","💗","👑","⭐","🏎","🌌","🐉","🪑","💞","🚀","💎","🪐"};
    private final int[] costs={50,200,1000,3000,5000,10000,19800,36000,68000,120000,168000,520000};
    private final int[] vip={0,0,1,2,3,5,8,12,18,25,35,45};'''
new='''    private final String[] names={"Rose","Heart","Crown","Star Castle","Royal Car","Galaxy","Golden Dragon","King Throne","Love Rain","Party Rocket","Diamond Crown","Universe","Golden Tiger","Phoenix Flight","Moon Palace","Crystal Swan","Neon Panther","Royal Yacht","Supercar","Golden Teapot","Gold Mask","Luxury Set","Magic Lamp","Peacock Dance","Ice Castle","Festival Drum","Fire Dragon","Angel Wings","Cosmic Whale","Royal Horse","Diamond Ring","Love Castle","VIP Jet","Meteor Shower","Lion King","KING Palace"};
    private final String[] icons={"🌹","💗","👑","⭐","🏎","🌌","🐉","🪑","💞","🚀","💎","🪐","🐯","🪽","🌙","🦢","🐆","🛥","🏎","🫖","🎭","🎁","🪔","🦚","🏰","🥁","🐲","🪽","🐋","🐎","💍","💖","✈️","☄️","🦁","🏯"};
    private final int[] costs={50,200,1000,3000,5000,10000,19800,36000,68000,120000,168000,520000,2500,7500,12000,16000,22000,28000,33000,3000,3000,10000,15000,18000,42000,26000,54000,36000,62000,45000,88000,98000,140000,180000,260000,620000};
    private final int[] vip={0,0,1,2,3,5,8,12,18,25,35,45,2,4,6,8,10,12,14,3,3,5,7,9,15,8,18,12,20,16,24,26,30,32,36,45};
    private final String[] categories={"Classic","Relationship","Royal","Classic","Flying","Flying","Royal","Royal","Relationship","Flying","Royal","Flying","Classic","Flying","Royal","Relationship","Classic","Flying","Flying","Classic","Classic","Royal","Classic","Classic","Royal","Activity","Royal","Relationship","Flying","Royal","Relationship","Relationship","Flying","Flying","Royal","Royal"};'''
if old not in s: raise SystemExit('gift arrays marker missing')
s=s.replace(old,new,1)
old='''tx.addView(tv("💎 "+costs[i]+"   •   "+(vip[i]==0?"Open":"VIP "+vip[i]),11,s.vipLevel>=vip[i]?0xffc9c3d2:0xffff8f8f,false),new LinearLayout.LayoutParams(-1,dp(24)));'''
new='''tx.addView(tv(categories[i]+"   •   💎 "+costs[i]+"   •   "+(vip[i]==0?"Open":"VIP "+vip[i]),11,s.vipLevel>=vip[i]?0xffc9c3d2:0xffff8f8f,false),new LinearLayout.LayoutParams(-1,dp(24)));'''
if old not in s: raise SystemExit('gift subtitle marker missing')
s=s.replace(old,new,1)
gift.write_text(s)

manifest=root/'app/src/main/AndroidManifest.xml'
s=manifest.read_text()
old='''        <activity android:name=".KingParityHubActivity" android:exported="false" />'''
new='''        <activity android:name=".KingParityHubActivity" android:exported="false" />
        <activity android:name=".KingEcosystemProActivity" android:exported="false" />'''
if old not in s: raise SystemExit('manifest parity marker missing')
manifest.write_text(s.replace(old,new,1))

rules=root/'firestore.rules'
s=rules.read_text()
old='''      match /messages/{messageId} {
        allow read: if familyMember(code) || familyOwner(code);
        allow create: if familyMember(code)
          && request.resource.data.uid == request.auth.uid
          && request.resource.data.text is string
          && request.resource.data.text.size() > 0
          && request.resource.data.text.size() <= 500;
        allow update, delete: if false;
      }
    }
'''
new='''      match /messages/{messageId} {
        allow read: if familyMember(code) || familyOwner(code);
        allow create: if familyMember(code)
          && request.resource.data.uid == request.auth.uid
          && request.resource.data.text is string
          && request.resource.data.text.size() > 0
          && request.resource.data.text.size() <= 500;
        allow update, delete: if false;
      }
      match /treasury/{entryId} {
        allow read: if familyMember(code) || familyOwner(code);
        allow create: if familyMember(code)
          && request.resource.data.uid == request.auth.uid
          && request.resource.data.type == 'points'
          && request.resource.data.amount is int
          && request.resource.data.amount >= 1
          && request.resource.data.amount <= 1000;
        allow update: if false;
        allow delete: if familyOwner(code);
      }
      match /activities/{activityId} {
        allow read: if familyMember(code) || familyOwner(code);
        allow create: if familyMember(code)
          && request.resource.data.uid == request.auth.uid
          && request.resource.data.type in ['checkin','activity']
          && request.resource.data.text is string
          && request.resource.data.text.size() > 0
          && request.resource.data.text.size() <= 300;
        allow update: if false;
        allow delete: if familyOwner(code);
      }
    }
'''
if old not in s: raise SystemExit('family rules marker missing')
rules.write_text(s.replace(old,new,1))
print('v8.8.0 ecosystem patch applied')
