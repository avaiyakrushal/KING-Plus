from pathlib import Path
import xml.etree.ElementTree as ET
import re
root=Path(__file__).resolve().parents[2]
java=root/'app/src/main/java/com/kingplus/social'
main=(java/'MainActivity.java').read_text();party=(java/'PartyActivity.java').read_text()
discover=(java/'DiscoverActivity.java').read_text();chat=(java/'ChatActivity.java').read_text()
assert int(re.search(r'versionCode\s+(\d+)',(root/'app/build.gradle').read_text()).group(1))>=346
assert "buildConfigField 'boolean', 'CLOUDFLARE_VOICE_ENABLED', 'false'" in (root/'app/build.gradle').read_text()
assert 'whereIn("category",aliases10067)' in party and 'lobbyPrevious10067' in party
assert 'Search all categories:' in party
assert '!lobbyFilter.trim().isEmpty()' in party and 'if(q.isEmpty()&&!categoryMatches(selected,category)) continue;' in party
assert 'tap Next to check more rooms' in party
assert 'Math.min(300,lobbyRoomLimit9989' not in party
assert 'if(!existing10067.exists())transaction10067.set' in main
assert 'Profile photo updated' not in main
assert 'ref.putBytes(jpeg,metadata)' in main and 'batch.commit().addOnSuccessListener' in main
assert 'profileWalletCoins.setText("◈ "+creditLabel10067)' in main
assert 'keyboard?View.GONE:View.VISIBLE' in party
assert 'clearPartyVisuals995();' in party
assert 'if(chatBox!=null)chatBox.removeAllViews();' in party
assert 'roomId = null; cloudRoom = false; memberSeen=false;' in party
leave=party[party.index('private void leaveRoom()'):party.index('    private void addBottomNav(',party.index('private void leaveRoom()'))]
lobby=party[party.index('private void renderLobby('):party.index('    private ',party.index('private void renderLobby(')+20)]
assert 'renderLobby(\"Hot\")' in leave
assert 'clearListeners();' in lobby and 'unregisterMember();' in lobby
assert 'pendingFollow10061.add(uid)' in discover
assert 'fields.put("followerName",followerName)' in discover
assert 'followerName.length()>60' in discover
assert 'db.runTransaction(transaction->' in discover
assert 'followAccountCurrent10028(owner)' in discover
assert 'readReceiptTarget10048' in chat and 'readReceiptPending10048' in chat
assert 'KingChatRead10068.canAcknowledge' in chat
assert 'messageQuery10068().addSnapshotListener' in chat
ET.parse(root/'app/src/main/res/layout/king_me_balances_10067.xml')
print('PASS Home/Me/Party cleanup, Discover follow dedup and Messages live-read guards')
