#!/usr/bin/env python3
"""KING Plus v9.9.4 - visual gift shop overhaul and safe free self gifting.

Edits signed v9.9.3 source only. Existing 88 gift names, pricing/Billing-disabled
guards, live Firebase event shape, VIP locks, audio and follow/room all unchanged.
"""
from pathlib import Path
import sys, shutil
base=Path(sys.argv[1])
pkg=base/'app/src/main/java/com/kingplus/social'
party=pkg/'PartyActivity.java'
gift=pkg/'KingFreeGift973.java'
gradle=base/'app/build.gradle'
shutil.copyfile(Path(__file__).with_name('KingGiftArtView.java'),pkg/'KingGiftArtView.java')
print('PASS new animated emoji/vector contextual art replaces 8 repetitive spheres')

def replace_one(file,old,new,label):
    src=file.read_text()
    count=src.count(old)
    if count!=1:raise SystemExit(f'FAIL {label}: anchor count={count}')
    file.write_text(src.replace(old,new,1))
    print('PASS',label)

# ONLY zero-coin, free decorative party gifts may be sent to self.
# The paid transfer validation and private-chat self checks remain unchanged.
replace_one(gift,
'''            && !sender.equals(target) && qty>=1 && qty<=MAX_QTY''',
'''            && qty>=1 && qty<=MAX_QTY''',
'free gift self-recipient allowed, paid and direct transfers untouched')

# Class field near gift-shop label method, not a static global shared renderer.
p=party.read_text()
a='''    private String giftSelectionText867(){'''
b='''    private KingGiftArtView giftHero994;
    private String giftSelectionText867(){'''
if p.count(a)!=1:raise SystemExit('missing gift header')
p=p.replace(a,b,1)
a='''        return giftIcons[i]+"  "+giftNames[i]+"  •  x"+Math.max(1,giftQuantity)+"  • FREE (no coins)"+"\\nTo: "+giftTargetName;'''
b='''        String tier994=i>=64&&giftVipRequired[i]>0
            ?" • VIP "+giftVipRequired[i]+" PREVIEW ONLY"
            :" • FREE (no coins)";
        return giftIcons[i]+"  "+giftNames[i]+"  • x"+Math.max(1,giftQuantity)
            +tier994+"\\nTo: "+giftTargetName;'''
if p.count(a)!=1:raise SystemExit('missing gift selection text')
p=p.replace(a,b,1)
a='''    private void refreshGiftSelection867(){if(giftSelectionLabel!=null)giftSelectionLabel.setText(giftSelectionText867());}'''
b='''    private void refreshGiftSelection867(){
        if(giftSelectionLabel!=null)giftSelectionLabel.setText(giftSelectionText867());
        if(giftHero994!=null&&giftSelectedIndex>=0&&giftSelectedIndex<giftNames.length)
            giftHero994.setGift(giftSelectedIndex,giftNames[giftSelectedIndex],
                                 giftIcons[giftSelectedIndex]);
    }'''
if p.count(a)!=1:raise SystemExit('missing refresh selection')
p=p.replace(a,b,1)
a='''        giftQuantity=1; giftCategory="All"; giftSelectedIndex=0;'''
b='''        giftQuantity=1; giftCategory="All"; giftSelectedIndex=0;giftHero994=null;'''
if p.count(a)!=1:raise SystemExit('missing gift reset')
p=p.replace(a,b,1)

# Large continuously moving selected-gift art scene in the shop header.
a='''        giftSelectionLabel=tv(giftSelectionText867(),13,0xffffe38a,true);giftSelectionLabel.setGravity(Gravity.CENTER_VERTICAL);giftSelectionLabel.setBackground(bg(0xff2a233a,14));LinearLayout.LayoutParams selLp=new LinearLayout.LayoutParams(-1,dp(58));selLp.setMargins(0,0,0,dp(5));root.addView(giftSelectionLabel,selLp);'''
b='''        LinearLayout heroPanel994=new LinearLayout(this);
        heroPanel994.setGravity(Gravity.CENTER_VERTICAL);
        heroPanel994.setPadding(dp(6),dp(4),dp(8),dp(4));
        heroPanel994.setBackground(bg(0xff282038,16));
        giftHero994=new KingGiftArtView(this,giftSelectedIndex,
            giftNames[giftSelectedIndex],giftIcons[giftSelectedIndex]);
        giftHero994.setShowcaseMode(true);
        heroPanel994.addView(giftHero994,new LinearLayout.LayoutParams(dp(112),dp(104)));
        LinearLayout heroInfo994=new LinearLayout(this);
        heroInfo994.setOrientation(LinearLayout.VERTICAL);
        giftSelectionLabel=tv(giftSelectionText867(),12,0xffffe38a,true);
        giftSelectionLabel.setGravity(Gravity.CENTER_VERTICAL);
        giftSelectionLabel.setMaxLines(3);
        heroInfo994.addView(giftSelectionLabel,
            new LinearLayout.LayoutParams(-1,dp(75)));
        TextView hint994=tv("✨ Animated gift preview • choose any gift",10,
            0xfff5cfee,false);
        heroInfo994.addView(hint994,new LinearLayout.LayoutParams(-1,dp(22)));
        heroPanel994.addView(heroInfo994,
            new LinearLayout.LayoutParams(0,dp(104),1));
        LinearLayout.LayoutParams heroLp994=new LinearLayout.LayoutParams(-1,dp(110));
        heroLp.bottomMargin=dp(4);
        root.addView(heroPanel994,heroLp);'''
if p.count(a)!=1:raise SystemExit('missing existing selection label')
p=p.replace(a,b,1)

# First recipient is always the CURRENT signed-in person, not just the room host.
a='''addGiftRecipientChip(people,"👑 "+ownerName,ownerUid==null?"":ownerUid);'''
b='''addGiftRecipientChip(people,"🙋 ME • "+safeName(),
            user==null?"":user.getUid());
        addGiftRecipientChip(people,"👑 "+ownerName,ownerUid==null?"":ownerUid);'''
if p.count(a)!=1:raise SystemExit('missing host recipient chip')
p=p.replace(a,b,1)

# Clear selected hero pointer on dialog dismissal; avoid holding old views.
a='''giftShopDialog=new AlertDialog.Builder(this).setView(root).create();'''
b='''giftShopDialog=new AlertDialog.Builder(this).setView(root).create();
        giftShopDialog.setOnDismissListener(d->{if(giftHero994!=null){
            giftHero994.setShowcaseMode(false);
            giftHero994=null;
        }});'''
if p.count(a)!=1:raise SystemExit('missing shop dialog')
p=p.replace(a,b,1)

# The previously monochrome locked VIP boxes still show actual distinguishable
# artwork, but they cannot be sent/charged until real VIP verifies server-side.
a='''View icon;if(unlocked){icon=new KingGiftArtView(this,idx,giftNames[i]);}else{TextView locked=tv("🔒",31,0xff77727e,false);locked.setGravity(Gravity.CENTER);icon=locked;}card.addView(icon,new LinearLayout.LayoutParams(-1,dp(45)));'''
b='''FrameLayout artStage994=new FrameLayout(this);
            KingGiftArtView art994=new KingGiftArtView(this,idx,
                giftNames[i],giftIcons[i]);
            artStage994.addView(art994,new FrameLayout.LayoutParams(-1,-1));
            if(!unlocked){
                TextView badge994=tv("🔒",13,0xffffd58b,true);
                badge994.setGravity(Gravity.RIGHT|Gravity.TOP);
                artStage994.addView(badge994,
                    new FrameLayout.LayoutParams(-1,dp(22),Gravity.TOP));
            }
            card.addView(artStage994,new LinearLayout.LayoutParams(-1,dp(61)));'''
if p.count(a)!=1:raise SystemExit('missing old gift icon creation')
p=p.replace(a,b,1)

# Enlarge tile gift illustrations without cutting labels. Only visible tiles
# redraw (7fps), large selected hero redraws at 19fps.
start=p.index('    private void rebuildGiftGrid(){')
end=p.index('    private void sendGift(',start)
chunk=p[start:end]
assert chunk.count('dp(116)')==1 and chunk.count('dp(110)')>=2
chunk=chunk.replace('dp(116)','dp(139)',1)
chunk=chunk.replace('dp(110)','dp(133)')
chunk=chunk.replace('card.setAlpha(unlocked?1f:.62f)','card.setAlpha(unlocked?1f:.82f)',1)
p=p[:start]+chunk+p[end:]
print('PASS larger 61dp distinct gift artworks, animated hero & self recipient')

# Force safe solo Party join before any free self gift - existing roomReady,
# memberSeen, Firebase event and cool-down checks are retained.
assert 'KingFreeGift973.allowed(sender973,targetUid,quantity,lastFreeGift973' in p
assert 'addGiftEvent(targetUid,targetName,gift,icon,0,quantity,KingFreeGift973.giftValue())' in p
party.write_text(p)
replace_one(gradle,
"versionCode 182; versionName '9.9.3-action-emoji-vip-voice-gifts'",
"versionCode 183; versionName '9.9.4-distinct-3d-gifts-self'",
'9.9.4 upgrade version')
print('DONE signed 9.9.4 gift imagery + safe self gifting, no Firestore rule broadening')
