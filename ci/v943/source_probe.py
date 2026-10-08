from pathlib import Path
import re,sys
root=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
targets={
 'RoomGameActivity.java':['private String roomId','private void renderState()','private void startRound','ListenerRegistration','result','onDestroy()'],
 'PartyActivity.java':['private void renderParty()','private void rebuildSeats()','private void showGiftEffect','private void showLiveEmojiEffect560','private void audioPkPanel700','private void ktvQueuePanel'],
 'OnlineLudoActivity.java':['private void render','private void rematch','phase','onDestroy()'],
}
for fn,needles in targets.items():
 p=root/fn
 print('\n########',fn,'########')
 if not p.exists(): print('MISSING'); continue
 lines=p.read_text(errors='replace').splitlines()
 for needle in needles:
  hits=[i for i,x in enumerate(lines) if needle in x]
  print('\n### NEEDLE',needle,'HITS',hits[:5])
  for i in hits[:2]:
   lo=max(0,i-8);hi=min(len(lines),i+40)
   for n in range(lo,hi): print(f'{n+1:05d}: {lines[n]}')
