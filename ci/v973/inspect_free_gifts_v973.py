#!/usr/bin/env python3
from pathlib import Path
import sys,re
root=Path(sys.argv[1]);p=root/'app/src/main/java/com/kingplus/social'
targets={
"PartyActivity.java":["    private void sendGiftQuantity(","    private void addGiftEvent(","    private void giftShopPanel(","    private void showGiftEffect(","    private void sendLiveEmojiV530(","    private void showLiveEmoji(","    private void openGift","    private void gift","    private void awardGiftProgress700(","giftCosts","giftVipRequired","giftNames","localCoins","setGift"],
"ChatActivity.java":["    private void sendDirectGift620(","    private boolean sendDirectGift620(","    private void directGiftPanel620(","    private void sendCloud(","    private void saveLocal(","    private void sendMessage(","    private void send(","private void sendDirectGift", "giftCosts620","giftNames620", "cloudMode", "testCoins", "private void","chatId"],
"KingVipVisualActivity.java":["    @Override public void onCreate(","    private void build(","    private void","verifiedWallet972","walletUnavailable972"],
"CommunityHubActivity.java":["    private void","    @Override","collection(\"families\")","collection(\"members\")"],
}
for fn, needles in targets.items():
 f=p/fn
 if not f.exists(): print("NO FILE",fn);continue
 s=f.read_text(errors='replace');lines=s.splitlines()
 print("==",fn,"size",len(s),"lines",len(lines))
 emitted=set()
 for term in needles:
  ids=[s.count("\n",0,m.start()) for m in re.finditer(re.escape(term),s)]
  print("SEARCH",repr(term),"COUNT",len(ids),"LINES",[i+1 for i in ids[:18]])
  for i in ids[:5]:
   n=43 if "sendGift" in term or "sendCloud" in term or "directGiftPanel" in term else (25 if ("private void" in term or "onCreate" in term) else 8)
   for k in range(max(0,i-2),min(len(lines),i+n)):
    if k not in emitted:
     print(f"{k+1}: {lines[k][:1300]}")
     emitted.add(k)
print("== GRADLE")
for line in (root/'app/build.gradle').read_text().splitlines():
 if "versionCode" in line:print(line)
