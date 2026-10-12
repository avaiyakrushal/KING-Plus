#!/usr/bin/env python3
"""Apply independent video-inspired VIP preview and task anti-abuse to verified v9.9.29 source.

Bolo Hi video is visual reference only. No proprietary assets, code, revenue or credits.
Fail closed on unexpected source; billing stays disabled.
"""
from pathlib import Path
import sys

root = Path(sys.argv[1]).resolve()
base = root / 'app/src/main/java/com/kingplus/social'
p = base / 'KingVipVisualActivity.java'
s = p.read_text()

def rep(old, new, label):
    global s
    if s.count(old) != 1:
        raise RuntimeError(f'{label}: expected one anchor, found {s.count(old)}')
    s = s.replace(old, new, 1)

rep('    private int verifiedLevel972;',
    '    private int verifiedLevel972;\n    private int selectedTier9930=-1;',
    'VIP preview state')
rep('boolean pink=vip>=25;',
    'int preview9930=selectedTier9930<0?vip:Math.max(0,Math.min(50,selectedTier9930));boolean pink=preview9930>=25;',
    'tier preview palette')
rep('int[] tierLevels={2,5,9,14,20,28,34,40,45,50};for(int t:tierLevels){boolean active=Math.abs(vip-t)<=2;',
    'for(int t=0;t<=LevelSystem.MAX_VIP;t++){boolean active=preview9930==t;',
    'tier list and active selection')
rep('TextView chip=tv("VIP"+t,12,active?0xff17100a:0xffe9e1ef,true);',
    'TextView chip=tv((t>vip?"🔒 ":"✓ ")+"VIP"+t,12,active?0xff17100a:0xffe9e1ef,true);chip.setOnClickListener(v->{selectedTier9930=t;build();});',
    'interactive tier selection')
rep('tiers.addView(tierRow);body.addView(tiers,new LinearLayout.LayoutParams(-1,dp(44)));',
    'tiers.addView(tierRow);body.addView(tiers,new LinearLayout.LayoutParams(-1,dp(44)));TextView proof9930=tv(verifiedWallet972?"My verified level: VIP"+vip+" • " + shownVipPoints972 + " recharge points":"Verified VIP data unavailable • Tier previews only",12,0xfff9e5ab,true);proof9930.setGravity(Gravity.CENTER);body.addView(proof9930,new LinearLayout.LayoutParams(-1,dp(40)));',
    'verified entitlement label')
rep('TextView badge=tv(vip>=40?"👑":vip>=28?"♦":vip>=14?"♛":"💠",58,Color.WHITE,false);',
    'TextView badge=tv(preview9930>=40?"👑":preview9930>=28?"♦":preview9930>=14?"♛":"💠",58,Color.WHITE,false);',
    'preview visual badge')
rep('TextView level=tv("VIP"+vip,28,Color.WHITE,true);',
    'TextView level=tv("VIP"+preview9930+(preview9930>vip?"  🔒":"  ✓"),28,Color.WHITE,true);',
    'preview tier title')
rep('TextView need=tv(vip>=LevelSystem.MAX_VIP?"MAX VIP":"Progress to VIP"+(vip+1),12,0xffeaf7ff,true);',
    'TextView need=tv(preview9930>vip?"Preview only • "+Math.max(0L,LevelSystem.vipThreshold(preview9930)-shownVipPoints972)+" verified points needed":vip>=LevelSystem.MAX_VIP?"MAX VERIFIED VIP":"Your current verified progress to VIP"+(vip+1),12,0xffeaf7ff,true);',
    'honest tier lock/status')
rep('TextView pack=tv("First VIP Reward Pack",14,0xffffe07b,true);',
    'TextView pack=tv("VIP"+preview9930+" Benefit Preview"+(preview9930>vip?" • LOCKED":""),14,0xffffe07b,true);',
    'reward previews not unlocked')
p.write_text(s)

# Stop no-evidence, forever-completing button taps from minting local XP.
# Buttons navigate to a real activity; verified task credit remains a future gate.
d = base / 'KingDeepFlowActivity.java'
t = d.read_text()
old = 'private void dailyTask(String name,String key,int xp){boolean done=prefs.getBoolean(key,false);row(done?"✅":"○",name,done?"Completed":"Reward +"+xp+" XP",()->{if(done){toast("Already completed");return;}prefs.edit().putBoolean(key,true).apply();LevelSystem.add(this,xp,0);render();});}'
new = '''private void dailyTask(String name,String key,int xp){boolean done=prefs.getBoolean(key,false);row(done?"✅":"○",name,done?"Previously claimed (legacy)":"Go to activity • +"+xp+" XP not yet verified",()->{toast("Opening activity • XP needs verified participation, not a button tap");if("task_party".equals(key)||"task_game".equals(key)){KingNav.openRoot(this,0);}else if("task_chat".equals(key)){KingNav.openRoot(this,3);}else if("task_gift".equals(key)){openGiftCenter();}else if("task_social".equals(key)){openCommunity("Moments");}});}'''
if t.count(old) != 1:
    raise RuntimeError('Task self-credit anchor missing or changed')
t = t.replace(old, new, 1)
d.write_text(t)

assert 'LIVE_RECHARGE = false' in (base/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (base/'KingPremiumAccess981.java').read_text()
assert 'verifiedWallet972?verifiedLevel972:0' in s
assert 'selectedTier9930=t;build()' in s
assert 'LevelSystem.add(this,xp,0)' not in t[t.index('private void dailyTask'):t.index('private void eventCenter')]
gradle = root / 'app/build.gradle'
g = gradle.read_text()
old_version = "versionCode 208; versionName '9.9.29-live-avatar-motion-test'"
if g.count(old_version) != 1:
    raise RuntimeError('Unexpected release version; refused to patch')
gradle.write_text(g.replace(old_version, "versionCode 209; versionName '9.9.30-video-vip-preview-test'", 1))
print('PASS VIP previews + verified locks + task anti-abuse, 209 upgrade, billing OFF')
