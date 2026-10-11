from pathlib import Path
base=Path('/tmp/v997/app/src/main/java/com/kingplus/social')
for filename,spans in [
 ('PartyActivity.java',[(1490,1590),(1680,1775),(1850,1960),(3498,3550),(3540,3615),(3760,3845),(3900,3985),(4410,4530)]),
 ('KingPublicProfileActivity.java',[(1,190)]),
 ('LevelSystem.java',[(1,180)]),
 ('RoomGameActivity.java',[(66,83),(140,175)])
]:
 f=base/filename
 if not f.exists():continue
 ls=f.read_text().splitlines()
 for start,end in spans:
  print('BLOCK',filename,start,end)
  for i in range(start-1,min(end,len(ls))):print(f'{i+1:05} {ls[i][:500]}')
