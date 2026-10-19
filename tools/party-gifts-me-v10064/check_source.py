from pathlib import Path
import re,json,xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[2]
java=root/'app/src/main/java/com/kingplus/social'
p=(java/'PartyActivity.java').read_text();me=(java/'MainActivity.java').read_text()
assert int(re.search(r'versionCode\s+(\d+)',(root/'app/build.gradle').read_text()).group(1)) >= 343
assert 'KingGiftPurchase10064.send(this,sendingRoom' in p
assert 'if(isFinishing()){\n' in p and 'clearVoiceRejoin9957();' in p
assert 'R.layout.king_me_identity_10064' in me and 'meIdentityListener10064.remove()' in me
assert 'R.layout.king_party_gift_header_10064' in p
assert 'R.layout.king_party_mic_seat_10064' in p
assert 'R.layout.king_party_host_action_10064' in p
assert 'paidGiftEvent10064(event)' in p
assert 'profileWalletCoins.setText("◈ "+creditLabel10067)' in me
for name in ['king_me_identity_10064.xml','king_party_gift_header_10064.xml','king_gift_history.xml','king_party_mic_seat_10064.xml','king_party_host_action_10064.xml']:
 ET.parse(root/'app/src/main/res/layout'/name)
catalog=(java/'KingGiftCatalog10064.java').read_text()
server=(root/'functions/gift-catalog.js').read_text()
names=json.loads('['+re.search(r'NAMES = \{(.*?)\};',catalog).group(1)+']')
costs=json.loads('['+re.search(r'COSTS = \{(.*?)\};',catalog).group(1)+']')
assert len(names)==len(costs)==64
for name in names:assert "'"+name+"'" in server
assert (root/'functions/room-gift-service.js').exists()
assert 'No pending gift.' in (java/'KingGiftPurchase10064.java').read_text()
print('PASS v10.0.64 source, XML integration, shared catalog, durable retry and lifecycle wiring')
