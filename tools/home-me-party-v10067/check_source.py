from pathlib import Path
import xml.etree.ElementTree as ET
import re
root=Path(__file__).resolve().parents[2]
java=root/'app/src/main/java/com/kingplus/social'
main=(java/'MainActivity.java').read_text();party=(java/'PartyActivity.java').read_text()
discover=(java/'DiscoverActivity.java').read_text();chat=(java/'ChatActivity.java').read_text()
assert int(re.search(r'versionCode\s+(\d+)',(root/'app/build.gradle').read_text()).group(1))>=346
hub=(java/'KingParityHubActivity.java').read_text()
home=hub[hub.index('    private void home(){'):hub.index('    private void openPartyLobby()',hub.index('    private void home(){'))]
for label in ('For You','Voice Rooms','Games','New People','VIP','Live Voice Rooms','Party Stage','Family','VIP & Rank'):
    assert label in home
for route in ('show("home")','openPartyLobby()','openPro880("home")','show("match")','show("vip")','show("stage")','show("family")'):
    assert route in home
assert 'Family & VIP' not in home
assert "buildConfigField 'boolean', 'CLOUDFLARE_VOICE_ENABLED', 'false'" in (root/'app/build.gradle').read_text()
assert 'whereIn("category",aliases10067)' in party and 'lobbyPrevious10067' in party
assert 'showAllLobbyCategories10099' in party and 'Browse Party categories' in party
assert '"All rooms","Hot","Event","Date","Music","KTV","Radio"' in party
assert '"Game","PK","Pick Me","Video","Family","Team Up"' in party
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
policy=(java/'KingPagePolicy10067.java').read_text()
for category in ('KTV','Radio','PK','Pick Me','Family','Team Up'):
    assert f'"{category}".equalsIgnoreCase(selected)' in policy
ET.parse(root/'app/src/main/res/layout/king_me_balances_10067.xml')
print('PASS Home/Me/Party cleanup, Discover follow dedup and Messages live-read guards')

# Multi Video safety/regression checks: server membership before RTC, default-muted media,
# authenticated seat ownership, and deterministic listener/transport cleanup.
video=(java/'KingMultiVideoActivity.java').read_text()
mic_consent=(java/'KingPartyMicConsent9956.java').read_text()
voice_reconnect=(java/'KingPartyVoiceReconnect9957.java').read_text()
assert 'get(com.google.firebase.firestore.Source.SERVER)' in video
assert 'serverVerified9921=true;' in video and 'startVerifiedVideo998(stage,loading);' in video
assert '.setAudioMuted(true).setVideoMuted(true)' in video
assert 'if(mySeat<1||!videoReady9921())' in video
assert 'if(!current.exists()||!uid.equals(current.getString("uid")))' in video
assert 'applyAudio940(mySeat<1||!micOn)' in video
assert 'applyVideo940(mySeat<1||!cameraOn)' in video
assert 'videoSeatListener9921.remove()' in video
assert 'meetView.dispose()' in video
# Activity teardown outside the explicit Leave path must also release its owned seat.
assert 'if(!leaving940&&mySeat>0)releaseVideoSeat9921(mySeat,null);' in video
assert 'approved&&cloud&&room!=null' in mic_consent
assert 'user.equals(seatOwner)' in mic_consent
assert 'pending&&optedIn&&foreground&&online&&cloudRoom&&memberSeen' in voice_reconnect

# PartyActivity must enforce consent and actual voice transport state at the audio boundary.
assert 'return KingPartyMicConsent9956.canTransmit(localMicConsent9956,cloudRoom,' in party
assert 'setInlineAudioMuted940(!canTransmitMic9956());' in party
assert 'boolean actualMuted9956=muted||!voiceConferenceConnected10065||!canTransmitMic9956();' in party
assert 'if(!desiredMic968)revokeMicConsent9956();' in party
assert 'if(!inRoomVoiceJoined940){' in party

