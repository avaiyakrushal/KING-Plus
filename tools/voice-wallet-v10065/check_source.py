from pathlib import Path
root=Path(__file__).resolve().parents[2]
j=root/'app/src/main/java/com/kingplus/social'
p=(j/'PartyActivity.java').read_text();m=(j/'MainActivity.java').read_text()
assert 'memberSeen=true;roomVoiceOptedIn957=soundOn;renderParty()' in p
assert 'voiceConferenceConnected10065=connected;' in p
assert 'd.put("voiceJoined",voiceConferenceConnected10065);' in p
assert '.setAudioMuted(true)' in p
assert 'muted||!voiceConferenceConnected10065||!canTransmitMic9956()' in p
assert 'session.equals(existing.getString("sessionId"))' in p
assert 'tx.update(member,changes)' in p
assert 'voiceMonitor10065.close()' in p
assert 'roomVoiceOptedIn957=soundOn' in p
assert 'stopInRoomVoice940(true);refreshMicControl()' in p
assert 'showVoiceConnection10065' in p
assert 'walletPageListener10065=firestore.collection("wallets")' in m
assert 'walletPageListener10065.remove()' in m
assert 'KingGiftReply10065.confirmed(raw,uid,requestId)' in (j/'KingGiftPurchase10064.java').read_text()
assert 'removeAuthStateListener(accountListener10065)' in (j/'KingGiftCenterActivity.java').read_text()
assert 'versionCode 344' in (root/'app/build.gradle').read_text()
print('PASS v10.0.65 voice connection proof, audience audio, sound off, membership update, wallet and gift wiring')
