from pathlib import Path
import sys
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social/PartyActivity.java'
lines=p.read_text(errors='replace').splitlines()
needles=['private void showGiftEffect','private void showLiveEmojiEffect560','private void pkBattle()','private void audioPkPanel700()','private void ktvQueuePanel()','private void giftShopPanel','private void showLiveEmojiPanelV530']
for needle in needles:
    hits=[i for i,x in enumerate(lines) if needle in x]
    print('\n###',needle,'hits',hits)
    for i in hits[:2]:
        lo=max(0,i-6);hi=min(len(lines),i+140)
        for n in range(lo,hi):print(f'{n+1:05d}: {lines[n]}')
