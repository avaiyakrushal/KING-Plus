#!/usr/bin/env python3
"""KING Plus v9.8.1: working gallery, verified VIP truth, real Gifts and Emoji sync.

Based strictly on the most recently compiled v9.8.0 games source.
- VIP progress is checked by KingBackend976; never award paid VIP from
  device-local test XP/coins/Gift activity.
- Frames/entrance effects have original visual previews, level gates,
  Firebase profile synchronization, and no fake premium entitlement.
- Old mock Room and old Main Gift Catalog no longer debit fake local coins.
  Real zero-cost Gifts continue through Party or Direct Chat.
- Emoji listener refuses previous Party session callbacks and no longer
  advertises FREE sticker packs as VIP-exclusive.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
for f in ('KingPremiumAccess981.java','KingFrameGallery981Activity.java'):
    shutil.copy2(Path(__file__).with_name(f),pkg/f)

def replace_once(s,a,b,label):
    n=s.count(a)
    if n!=1:raise SystemExit(f'{label}: source marker count {n}, expected 1: {a[:95]!r}')
    print('PASS',label)
    return s.replace(a,b,1)

def method_replace(s,start_text,new,label):
    a=s.find(start_text)
    if a<0 or s.count(start_text)!=1:raise SystemExit(f'{label}: method not unique')
    b=s.find('{',a);depth=0;quote=None;esc=False
    for k in range(b,len(s)):
        c=s[k]
        if quote:
            if esc:esc=False
            elif c=='\\':esc=True
            elif c==quote:quote=None
        elif c in ('"',"'"):quote=c
        elif c=='{':depth+=1
        elif c=='}':
            depth-=1
            if depth==0:
                print('PASS',label)
                return s[:a]+new+s[k+1:]
    raise SystemExit(f'{label}: unbalanced braces')

main=pkg/'MainActivity.java'
s=main.read_text()
s=method_replace(s,'    private void selectCosmetic730(boolean frame){',
r'''    private void selectCosmetic730(boolean frame){
        Intent i=new Intent(this,KingFrameGallery981Activity.class);
        i.putExtra("frames",frame);
        startActivity(i);
    }''','Frame and Entrance Effect buttons open functional gallery with preview/equip/sync')

s=method_replace(s,'    private void vipPage(){',
r'''    private void vipPage(){
        startActivity(new Intent(this,KingVipVisualActivity.class));
    }''','legacy fake/test-paid VIP panel opens verified recharge-only VIP')

s=method_replace(s,'    private void levelPage(){',
r'''    private void levelPage(){
        screen="level";
        base("Level & Achievements","KING Plus normal activity levels • VIP is recharge-only");
        LevelSystem.Snapshot p=LevelSystem.read(this);
        long next=p.nextLevelXp();
        long prev=LevelSystem.levelThreshold(p.level);
        long span=Math.max(1,next-prev);
        int percent=p.level>=LevelSystem.MAX_LEVEL?100:
            (int)Math.max(0,Math.min(100,(p.xp-prev)*100/span));
        text("🏆 Normal Level "+p.level+" / 99",28,Color.WHITE,true);
        text(LevelSystem.levelTier(p.level)+" • "+p.xp+" XP • "+percent+"% to next",15,MUTED,false);
        button("👑 Verified Recharge VIP",PURPLE,
            ()->startActivity(new Intent(this,KingVipVisualActivity.class)));
        text("VIP cannot increase from free Gifts, offline coins, or local TEST activity.",
            13,MUTED,false);
        text("Achievements",18,Color.WHITE,true);
        text("🎤 Voice Explorer  "+(getMissionValue("mission_room")>0?"✓":"○"),16,Color.WHITE,false);
        text("💬 Room messages today  "+getMissionValue("mission_chat"),16,MUTED,false);
        button("🎒 Collection & Frames",PURPLE,()->openCommunityHub700("Collection"));
        button("Back",CARD,this::profile);
    }''','normal Level and verified paid VIP now separate')

s=method_replace(s,'    private void giftCatalogPage(){',
r'''    private void giftCatalogPage(){
        screen="gift_catalog";
        base("KING Gift Center","FREE live room and chat Gifts • zero spend in this build");
        text("🌹 💝 👑 🎉 💎 🎁",32,Color.WHITE,true);
        text("FREE Gifts and Live Emoji are sent through your signed-in Party Room or Private Chat. A recipient must actually be connected.",15,MUTED,false);
        button("🎁 Open Live Party Gift Shop",PURPLE,this::openPartyActivity);
        button("💬 Send FREE Gift in Private Chat",CARD,this::messages);
        text("Paid Diamond recharge and VIP privileges remain disabled until secure payment verification is ready. TEST coins are not real Diamonds.",13,MUTED,false);
        button("Back to Wallet",CARD,this::walletPage);
    }''','remove fake Main Gift Catalog prices; route to real Firebase Party/Chat Gift')

s=method_replace(s,'    private void giftDialog(LinearLayout chat){',
r'''    private void giftDialog(LinearLayout chat){
        new AlertDialog.Builder(this)
            .setTitle("🎁 FREE KING Gifts")
            .setMessage("Real-time gifts need a signed-in recipient. Open a live Party or Private Chat to send FREE decorative gifts. This device-only room cannot transfer Diamonds.")
            .setPositiveButton("Live Party",(d,w)->openPartyActivity())
            .setNeutralButton("Private Chat",(d,w)->messages())
            .setNegativeButton("Close",null).show();
    }''','legacy offline Room gifts now route to synced Gift screens')

s=method_replace(s,'    private void sendGift(LinearLayout chat,String gift,int price,String target){',
r'''    private void sendGift(LinearLayout chat,String gift,int price,String target){
        // Never subtract locally invented coins or claim a gift was sent online.
        new AlertDialog.Builder(this).setTitle("FREE Gift • "+gift)
            .setMessage("Select the actual KING Plus recipient in a live Party or Private Chat. No local TEST coins are charged.")
            .setPositiveButton("Open Party",(d,w)->openPartyActivity())
            .setNegativeButton("Close",null).show();
    }''','remove fake Main Room Gift spending and false online delivery')

# Avoid publishing forged locally earned VIP to public_profiles.
s=replace_once(s,'profile.put("vipLevel",progress.vipLevel);','',
 'Main profile sync does not publish TEST VIP')
s=replace_once(s,'profile.put("vipPoints",progress.vipPoints);','',
 'Main profile sync does not publish TEST VIP points')
main.write_text(s)

party=pkg/'PartyActivity.java'
p=party.read_text()
p=replace_once(p,
 'final String activeUserForEmoji960=user.getUid();',
 'final String activeUserForEmoji960=user.getUid();\n        final int activeEmojiSession981=presenceGeneration967;',
 'capture immutable session generation for Live Emoji listeners')
p=replace_once(p,
 'if(isFinishing()||isDestroyed()||!cloudRoom||roomId==null||!activeRoomForEmoji960.equals(roomId))return;',
 'if(!isActivePresence967(activeRoomForEmoji960,activeUserForEmoji960,activeEmojiSession981))return;',
 'prevent delayed previous-room Emoji events reaching new room')
p=replace_once(p,
 '"👑 VIP Exclusive Stickers  •  KING original pack"',
 '"✨ KING Original Pack  •  FREE animations"',
 'Emoji pack describes real free availability, not fake VIP exclusivity')
p=replace_once(p,'d.put("vipLevel",p720.vipLevel);',
               'd.put("vipLevel",0);',
               'Party membership no longer claims device-local paid VIP entitlement')
p=replace_once(p,'m.put("vipLevel",p.vipLevel);','',
 'Progress sync avoids paid VIP forgery')
p=replace_once(p,'m.put("vipPoints",p.vipPoints);','',
 'Progress sync avoids paid VIP point forgery')
p=replace_once(p,'event971.put("actorVip",progress971.vipLevel);',
               'event971.put("actorVip",0);',
               'Free Gift events never advertise paid VIP from TEST points')
p=replace_once(p,'message971.put("senderVip",progress971.vipLevel);',
               'message971.put("senderVip",0);',
               'Free Gift chat messages never advertise paid VIP from TEST points')
p=replace_once(p,'vip720=me720.vipLevel;',
               'vip720=KingPremiumAccess981.PAID_VIP_ACTIVE?me720.vipLevel:0;',
               'My Party seat VIP badge cannot be unlocked by local TEST values')
p=replace_once(p,'d.put("actorVip",p720.vipLevel);',
               'd.put("actorVip",0);',
               'Party event sender VIP defaults verified-safe until purchase backend active')
party.write_text(p)

vip=pkg/'KingVipVisualActivity.java'
v=vip.read_text()
v=replace_once(v,
 'Your VIP level is calculated using server-confirmed gifts in the secure wallet.',
 'Your VIP level increases only after verified Diamond Recharge, never from sending FREE Gifts.',
 'VIP info accurately explains recharge-only verified progression')
vip.write_text(v)

manifest=root/'app/src/main/AndroidManifest.xml'
m=manifest.read_text()
if 'android:name=".KingFrameGallery981Activity"' not in m:
    old='<activity android:name=".KingVipVisualActivity" android:exported="false" />'
    if m.count(old)!=1:raise SystemExit('missing VIP Activity manifest anchor')
    m=m.replace(old,old+'\n        <activity android:name=".KingFrameGallery981Activity" android:exported="false" />')
    manifest.write_text(m)
    print('PASS install nonexported Frame Gallery Activity')
else:print('PASS existing Gallery manifest')

gradle=root/'app/build.gradle'
g=gradle.read_text()
old="versionCode 171; versionName '9.8.0-playable-games'"
if g.count(old)!=1:raise SystemExit('Expected last successful v9.8.0 game APK source')
gradle.write_text(g.replace(old,"versionCode 172; versionName '9.8.1-games-vip-frames-gifts-emoji'",1))
print('PASS KING Plus v9.8.1 games and cosmetics final patch completed')
