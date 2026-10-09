#!/usr/bin/env python3
from pathlib import Path
import re,sys
root=Path(sys.argv[1]); pkg=root/'app/src/main/java/com/kingplus/social'
files=sorted(pkg.glob('*.java'))
print("ALL_CLASS_NAMES", " ".join(p.name for p in files))
def extract(s,needle,maxchars=18000):
 at=s.find(needle)
 if at<0:return 'NOT FOUND '+needle
 # locate start of block after signature
 begin=s.find('{',at);depth=0;i=begin
 if begin<0:return 'NO BRACE '+needle
 quote=None;esc=False
 while i<len(s):
  ch=s[i]
  if quote:
   if esc:esc=False
   elif ch=='\\':esc=True
   elif ch==quote:quote=None
  else:
   if ch in ('"',"'"):quote=ch
   elif ch=='{':depth+=1
   elif ch=='}':
    depth-=1
    if depth==0:break
  i+=1
 return f"START_LINE {s.count(chr(10),0,at)+1} CHARS {i-at}\n"+s[at:min(i+1,at+maxchars)]
targets={
'PartyActivity.java':[
'private void sendGiftQuantity(','private void addGiftEvent(','private void sendGiftTo(',
'private void showGiftEffect(','private void sendLiveEmojiV530(',
'private void attachLiveEmojiListenerV530(', 'private void attachCloudRoom(',
'private void showLiveEmojiEffect560(', 'private void addEvent(',
'private void giftShopPanel(','private void sendStickerOrEmoji(',
'private void giftHistoryDialog(', 'private void familyPartyPanel610(',
'private void shareFamilyRoom(', 'private void openCommunityHub700('],
'ChatActivity.java':['private boolean sendDirectGift620(','private void directGiftPanel620(',
'private void render(', 'private void sendCloud(','private void sendMessage(',
'private void loadCloud(', 'private void cloudMessage(',
'private void syncDirectProgress700(', 'private void directGiftHistory620('],
'KingVipVisualActivity.java':['private void render(','private void','public void onCreate('],
'LevelSystem.java':['public static Snapshot gift(', 'public static Snapshot read(', 'private static','public static long vipThreshold('],
'CloudBackend.java':['static void sendGift(', 'void sendGift(', 'sendGift(', 'class CloudBackend'],
'CommunityHubActivity.java':['private void family(', 'private void familyChat(', 'private void familyJoinCreate(', 'private void createFamily(', 'private void joinFamily(', 'private void showFamily(', 'private void sendFamilyMessage(', 'private void familyMembers(', 'private void familyLuckyBag940(', 'private void leaveFamily('],
'MainActivity.java':['private void family(', 'private void showFamily(', 'private void openFamily(', 'Family', 'KingVipVisualActivity'],
'InboxActivity.java':['private void newChatDialog(', 'private void openChat('],
}
for f,keys in targets.items():
 p=pkg/f
 if not p.exists():continue
 s=p.read_text(errors='replace')
 print('\n===== FILE',f,'LEN',len(s),'=====')
 for key in keys:
  n=s.count(key)
  print('FIND',repr(key),'COUNT',n)
  if n>0:
   print(extract(s,key,18500))
print("=== FAMILY/CLOUD COLLECTION CALLS ===")
for p in files:
 t=p.read_text(errors='replace')
 if 'collection("families")' in t or 'collection("family' in t or 'family_code' in t:
  print('FAMILY_FILE',p.name, 'calls',t.count('collection("families")'), 'prefs',t.count('family_code'))
  lines=t.splitlines()
  for i,row in enumerate(lines):
   if any(z in row for z in ['collection("families")','family_code','familyName','familyCode']):
    print(f"FAMILY_LINE {i+1}: {row[:1800]}")
