#!/usr/bin/env python3
from pathlib import Path
import re,sys
pkg=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
queries={
 'PartyActivity.java':['private void sendLiveEmojiV530(','private void attachLiveEmojiListenerV530(',
 'private void renderLiveEmojiV530(','private void showLiveEmojiEffect560(',
 'private void showGiftEffect(','private void addGiftEvent(','private void attachV530RealtimeHelpers(',
 'private void showEntranceEffect730(',
 'private void giftShopPanel(',
 'private void sendGiftQuantity(',
 'private void renderGift','private void handleEvent','private void showLiveEmoji',
 'private void liveEmojiPanelV530(',
 'private void playLiveEmoji'],
 'KingVipVisualActivity.java':['@Override public void onCreate(','private void build('],
 'MainActivity.java':['private void syncPublicProfile(', 'private void vipPage(', 'private void levelPage(',
 'private void giftCatalogPage(', 'private void sendGift(',
 'private void selectCosmetic730('],
 'KingGiftCenterActivity.java':['onCreate(', 'private void'],
 'KingCosmetics.java':['public static boolean unlockedFrame(']
}
for name,keys in queries.items():
 f=pkg/name
 if not f.exists():print('MISSING',name);continue
 s=f.read_text(errors='replace');lines=s.splitlines()
 print('===== CLASS',name,'TOTAL',len(s),'LINES',len(lines))
 covered=set()
 for needle in keys:
  hits=[s.count('\n',0,m.start()) for m in re.finditer(re.escape(needle),s)]
  print('FIND',repr(needle),'COUNTS',len(hits),'LINES',[i+1 for i in hits[:8]])
  for i in hits[:3]:
   pre=4
   follow=18 if name=='MainActivity.java' else (45 if name=='PartyActivity.java' else 15)
   if needle in ('private void giftShopPanel(','private void showEntranceEffect730(','private void sendGiftQuantity(','private void addGiftEvent('):
    follow=65
   for n in range(max(0,i-pre),min(len(lines),i+follow)):
    if n not in covered:
     print(f'{n+1}: {lines[n][:2400]}')
     covered.add(n)
