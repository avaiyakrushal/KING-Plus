from pathlib import Path
import sys
root=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
targets={
 'PartyActivity.java':['showGiftEffect(','showLiveEmojiEffect560(','audioPkPanel700()','ktvQueuePanel()','giftShopPanel','showLiveEmojiPanelV530','KingGiftArtView'],
 'CommunityHubActivity.java':['private void collection()','private void relationships()','private void visitors750()','communityRetry945'],
 'KingVipVisualActivity.java':['private void build()','See More','rewards'],
 'MainActivity.java':['private void loadRealProfileData()','addProfileRecommendations940','loadPartyRecordsV600'],
}
for fn,needles in targets.items():
 p=root/fn; print('\n########',fn,'########')
 if not p.exists(): print('MISSING');continue
 lines=p.read_text(errors='replace').splitlines()
 for needle in needles:
  hits=[i for i,x in enumerate(lines) if needle in x]
  print('\n###',needle,'hits',hits[:6])
  for i in hits[:2]:
   lo=max(0,i-8);hi=min(len(lines),i+80)
   for n in range(lo,hi): print(f'{n+1:05d}: {lines[n]}')
