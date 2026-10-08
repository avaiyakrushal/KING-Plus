from pathlib import Path
import sys
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social/RoomGameActivity.java'
lines=p.read_text(errors='replace').splitlines()
for needle in ['private String roomId','ListenerRegistration','private void startRound','private void renderState()','private void openPartyLudo862','private void showGameHistory940','onDestroy()']:
    hits=[i for i,x in enumerate(lines) if needle in x]
    print('\n###',needle,'hits',hits[:4])
    for i in hits[:2]:
        lo=max(0,i-5);hi=min(len(lines),i+75)
        for n in range(lo,hi): print(f'{n+1:05d}: {lines[n]}')
