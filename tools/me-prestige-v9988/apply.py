#!/usr/bin/env python3
"""KING v9.9.88 Me: no fake paid VIP/Noble from local gift/test points.

Apply to complete last signed v9.9.87 source (vCode266). Never activate paid
Recharge/VIP or alter Agency, CP, Family, Party and Games settlement.
"""
from pathlib import Path
import re,sys,shutil
r=Path(sys.argv[1]);java=r/'app/src/main/java/com/kingplus/social'
here=Path(__file__).parent
for name in ('KingPrestigeIdentity9988.java','KingMeVerifiedPrestige9988Activity.java'):
 shutil.copyfile(here/name,java/name)
def update(path,work):
 p=java/path;s=p.read_text();n=work(s);p.write_text(n)
def swap(s,old,new):
 assert s.count(old)==1,'Source changed or ambiguous: '+old[:130]
 return s.replace(old,new,1)
manifest=r/'app/src/main/AndroidManifest.xml'
s=manifest.read_text()
s=swap(s,
 '<activity android:name=".KingVipVisualActivity" android:exported="false" />',
 '<activity android:name=".KingVipVisualActivity" android:exported="false" />\n'
 '        <activity android:name=".KingMeVerifiedPrestige9988Activity" android:exported="false" />')
manifest.write_text(s)

rules=r/'firestore.rules';s=rules.read_text()
s=swap(s,'    match /public_profiles/{uid} {',
'''    match /public_profiles/{uid} {''')
# A public profile is an editable *identity*, not a server-authorized VIP
# ledger. Client-authored badges above VIP0/Noble0 are forbidden.
s=swap(s,'''      allow create, update: if owner(uid)
        && request.resource.data.uid == uid;''',
'''      allow create, update: if owner(uid)
        && request.resource.data.uid == uid
        && request.resource.data.get('vipLevel',0) == 0
        && request.resource.data.get('vipPoints',0) == 0
        && request.resource.data.get('nobleLevel',0) == 0;''')
s=swap(s,'    match /wallets/{uid} {',
 (here/'prestige-firestore.rules').read_text()+'\n    match /wallets/{uid} {')
rules.write_text(s)
# Profile syncs must not cause Firestore rules to reject unrelated edits.
def social(s):
 return s.replace('patch970.put("vipLevel",levels970.vipLevel);','patch970.put("vipLevel",0);').replace(
   'patch970.put("vipPoints",levels970.vipPoints);','patch970.put("vipPoints",0L);')
update('SocialActivity.java',social)
def chat(s):
 return s.replace('m.put("vipLevel",p.vipLevel);m.put("vipPoints",p.vipPoints);',
                  'm.put("vipLevel",0);m.put("vipPoints",0L);').replace(
  'peerNameView940.setText(cloudName+"   VIP "+(vip==null?0:vip)+" · Lv."+(lv==null?1:lv))',
  'peerNameView940.setText(cloudName+" · Activity Lv."+(lv==null?1:lv)+" · VIP unverified")')
update('ChatActivity.java',chat)
def wardrobe(s):
 s=swap(s,'progress=LevelSystem.read(this);',
'''LevelSystem.Snapshot local=LevelSystem.read(this);
        // TEST local VIP gift points cannot unlock paid Royal/Crown cosmetics.
        progress=new LevelSystem.Snapshot(local.level,local.xp,0,0);''')
 s=swap(s,'m.put("level",progress.level);m.put("vipLevel",progress.vipLevel);',
            'm.put("level",progress.level);m.put("vipLevel",0);m.put("vipPoints",0L);')
 s=s.replace('Unlock cosmetics by normal Level / VIP progress.',
   'Free level cosmetics unlock with activity XP. Paid VIP cosmetics remain unavailable until entitlement verification.')
 return s
update('KingWardrobeActivity.java',wardrobe)
def deep(s):
 s=swap(s,'case "vip_rules": vipRules();break; case "noble": nobleCenter();break;',
 '''case "vip_rules": vipRules();break;
            case "noble": startActivity(new Intent(this,KingMeVerifiedPrestige9988Activity.class));finish();break;''')
 s=swap(s,'''hero("👤  "+safe(mainPrefs.getString("name","KING User"),"KING User"),"VIP "+s.vipLevel+" • Lv."+s.level+" • KING Plus identity");''',
  '''hero("👤  "+safe(mainPrefs.getString("name","KING User"),"KING User"),
    "Activity Lv."+s.level+" (TEST) • Verified VIP in Wallet");''')
 s=swap(s,'''hero("🎁 Privilege Pack","VIP "+s.vipLevel+" • Lv."+s.level+" • your unlocked KING identity");''',
  '''hero("🎁 Privilege Pack","Activity Lv."+s.level+" • Paid VIP cosmetics require verified server entitlement");''')
 s=swap(s,'''    private void vipRules(){hero("VIP Rules","KING Plus VIP progression is tied to supported activity and gifting in the current no-billing/test build.");''',
  '''    private void vipRules(){hero("Verified VIP Rules","Paid VIP ONLY rises after validated Recharge in trusted Wallet Server. Local free gifts, XP or TEST coins grant no VIP.");''')
 s=s.replace('chip("VIP 0–12 keeps the original early thresholds",()->{});',
  'chip("VIP 0–50 preview: account proof comes from authenticated Wallet",()->{});')
 s=s.replace('chip("VIP 13–50 uses progressive KING tiers",()->{});',
  'chip("VIP/Noble entitlements cannot be bought or granted by this TEST app",()->{});')
 s=s.replace('chip("VIP does not decrease automatically in this build",()->{});',
  'chip("No production billing or unverified VIP benefits enabled",()->{});')
 s=s.replace('boolean vip=i>=3;int cur=vip?s.vipLevel:s.level;',
   'boolean vip=i>=3;int cur=vip?0:s.level;') # Local paid medals are NOT earned.
 s=s.replace('hero("🏅 Medals & Influence","Unlocked from levels, VIP, social and room activity");',
   'hero("🏅 Medals & Influence","Activity milestones are TEST; paid VIP medals require verified entitlement");')
 # Store cosmetic previews with no local paid entitlement.
 s=s.replace('KingCosmetics.unlockedFrame(f,LevelSystem.read(this))',
    'KingCosmetics.unlockedFrame(f,freeActivityOnly9988())')
 s=s.replace('KingCosmetics.unlockedEffect(e,LevelSystem.read(this))',
    'KingCosmetics.unlockedEffect(e,freeActivityOnly9988())')
 s=s.replace('KingCosmetics.unlockedBubble(b,LevelSystem.read(this))',
    'KingCosmetics.unlockedBubble(b,freeActivityOnly9988())')
 insert='''    private LevelSystem.Snapshot freeActivityOnly9988(){
        LevelSystem.Snapshot raw=LevelSystem.read(this);
        return new LevelSystem.Snapshot(raw.level,raw.xp,0,0);
    }
'''
 anchor='    private void equipFrame(String f){'
 assert anchor in s
 s=s.replace(anchor,insert+anchor,1)
 return s
update('KingDeepFlowActivity.java',deep)
def main(s):
 s=swap(s,'TextView vip=smallBadge("VIP "+progress700.vipLevel,0xffffe57b,0xff5a4100);',
 '''TextView vip=smallBadge("VIP …",0xffffe57b,0xff5a4100);
        loadVerifiedMeVip9988(vip);''')
 anchor='    private void loadRealCoupleCount9985(TextView counter){'
 fn='''    /** Paid VIP badge reads from the authenticated wallet server, NOT local gifts. */
    private void loadVerifiedMeVip9988(TextView badge){
        if(badge==null)return;
        if(firebaseAuth==null||firebaseAuth.getCurrentUser()==null){
            badge.setText("VIP —");return;
        }
        final String currentUid=firebaseAuth.getCurrentUser().getUid();
        KingBackend976.fetchWallet(this,(wallet,error)->{
            if(isFinishing()||isDestroyed()||firebaseAuth==null||
                firebaseAuth.getCurrentUser()==null||
                !currentUid.equals(firebaseAuth.getCurrentUser().getUid()))return;
            badge.setText(wallet!=null &&
                KingPrestigeIdentity9988.validVip(wallet.vipLevel)
                ?"VIP "+wallet.vipLevel:"VIP —");
        });
    }

'''
 assert anchor in s
 s=s.replace(anchor,fn+anchor,1)
 return s
update('MainActivity.java',main)
def public(s):
 # Never present user-written VIP/Noble fields as trusted paid perks.
 s=swap(s,'Long lv=p.getLong("level"),vip=p.getLong("vipLevel"),xp=p.getLong("xp"),vipPoints=p.getLong("vipPoints");',
   'Long lv=p.getLong("level"),vip=0L,xp=p.getLong("xp"),vipPoints=0L;')
 s=swap(s,
    'TextView lvLine=tv("VIP "+(vip==null?0:vip)+"     Lv."+(lv==null?1:lv),12,PURPLE,true);',
    'TextView lvLine=tv("VIP not verified  ·  Activity Lv."+(lv==null?1:lv),12,PURPLE,true);')
 a=s.index('        // Noble level is not a paid entitlement until a trusted server ledger exists.')
 b=s.index('        TextView nobleBadge997=tv(',a)
 s=s[:a]+'''        // Public paid prestige requires a trusted backend grant, never self-authored profile fields.
        String nobleText=KingPrestigeIdentity9988.nobleLabel(false,0);
'''+s[b:]
 anchor='        body.addView(nobleBadge997,nobleLp997);'
 assert anchor in s
 s=s.replace(anchor,anchor+'''
        final String prestigeUid9988=uid;
        db.collection("king_public_prestige").document(prestigeUid9988)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(grant->{
                if(isFinishing()||isDestroyed()||!prestigeUid9988.equals(uid))return;
                if(!grant.exists())return;
                Long v=grant.getLong("vipLevel"),n=grant.getLong("nobleLevel");
                Long total=grant.getLong("rechargeTotal");
                if(v==null||n==null||total==null||
                    !KingPrestigeIdentity9988.allowedVerifiedPublic(
                        v.intValue(),n.intValue(),total.longValue()))return;
                lvLine.setText("✓ Verified VIP "+v+" · Activity Lv."+(lv==null?1:lv));
                nobleBadge997.setText("✓ Verified Noble "+n+" (server record)");
            }).addOnFailureListener(err->{
                // Never substitute unverified local state on network error.
            });''',1)
 s=s.replace('row("⭐ Level & VIP","XP "+(xp==null?0:xp)+" • VIP "+(vip==null?0:vip)+" • "+(vipPoints==null?0:vipPoints)+" points",()->openDeep("vip"));',
    'row("⭐ Levels & VIP","Activity XP is TEST • VIP only verified by server",()->openDeep("vip"));')
 s=s.replace('Long lv=p.getLong("level"),vip=p.getLong("vipLevel"),xp=p.getLong("xp"),vp=p.getLong("vipPoints");',
    'Long lv=p.getLong("level"),vip=0L,xp=p.getLong("xp"),vp=0L;')
 s=s.replace('+"\\\\n💎 VIP "+(vip==null?0:vip)+" • "+(vp==null?0:vp)+" points"',
    '+"\\\\n💎 VIP requires trusted server verification"')
 # remove paid medal badges from arbitrary profile fields
 s=s.replace('((vip==null?0:vip)>=3?"💎 VIP Supporter  ":"")+((vip==null?0:vip)>=8?"👑 Royal Patron":"")',
    '""')
 return s
update('KingPublicProfileActivity.java',public)
def party(s):
 s=swap(s,
 'if(after.vipLevel>before.vipLevel)toast("💎 VIP up • VIP "+after.vipLevel);',
 '/* FREE gift TEST XP does not award verified VIP or paid Noble */')
 return s
update('PartyActivity.java',party)
gradle=r/'app/build.gradle'
g=gradle.read_text()
g,n=re.subn(r"versionCode\s+266\s*;\s*versionName\s+'[^']+'",
   "versionCode 267; versionName '9.9.88-me-verified-vip-noble-test'",g)
assert n==1,'Must restore complete signed v9.9.87 source'
gradle.write_text(g)
assert 'KingFamilyModeration9987' in (java/'CommunityHubActivity.java').read_text()
assert 'KingAgency9982Activity' in manifest.read_text()
assert 'KingMeGiftSummary9983' in (java/'KingDeepFlowActivity.java').read_text()
assert 'LIVE_RECHARGE = false' in (java/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (java/'KingPremiumAccess981.java').read_text()
print('PASS paid VIP tied only to verified wallet, Noble server-only previews, public spoof rejection and Me/Family preserved')
