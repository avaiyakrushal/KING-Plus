from pathlib import Path
root=Path('/tmp/v997/app/src/main/java/com/kingplus/social')
name='PartyActivity.java'
ls=(root/name).read_text().splitlines()
for key in ['private void openSeatProfile(', 'private void showOccupiedSeatMenuV530(', 'private void administratorDialog(', 'private void chooseAdministrator(', 'private void openRoomGames740(', 'private void membersDialog(', 'private void memberProfileDialog(', 'private void memberUserMenu(', 'private void onDestroy(', 'private void showRichProfile(', 'private void coHostDialog(']:
 starts=[i for i,l in enumerate(ls) if key in l]
 for i in starts[:2]:
  print('KEY',key,'line',i+1)
  for j in range(i,min(len(ls),i+22)):print(f'{j+1:05} {ls[j][:650]}')
ls=(root/'KingPublicProfileActivity.java').read_text().splitlines()
for i in range(0,min(72,len(ls))):print('PROFILE',i+1,ls[i][:650])
