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
for route in ('show("home")','openPartyLobby()','KingNav.openRoot(this,2)','KingNav.openRoot(this,3)','show("vip")','show("stage")','show("family")'):
    assert route in home
assert home.count('KingNav.openRoot(this,2)') == 2
assert '"New People","discover"' in home
assert '"Meet New People","Discover KING Plus members and profiles",()->KingNav.openRoot(this,3)' in home
assert 'Family & VIP' not in home
assert 'addDiscoverEntrance940(row,"🎮","Games","Games and multiplayer rooms",()->KingNav.openMainTab(this,1));' in discover
assert 'KingNav.bottomBar(this,3' in discover
assert 'str(doc,"coverUrl",str(doc,"roomPhotoUrl",str(doc,"photoUrl","")))' in party
assert 'roomText(doc,"coverUrl",roomText(doc,"roomPhotoUrl",""))' in hub
assert 'roomText(doc,"coverUrl",roomText(doc,"ownerPhoto",""))' not in hub
create=party[party.index('private void createCloudRoom(String name,String category,int seats,boolean priv)'):party.index('    private void finishRoomCreate921',party.index('private void createCloudRoom(String name,String category,int seats,boolean priv)'))]
assert 'String entranceMode=priv?"invite":"free";' in create
assert create.count('put("entranceMode",entranceMode)') == 3
assert create.count('put("entryEpoch",entryEpoch)') == 3
assert 'private void retryRoomCreateCompact921(DocumentReference ref,String name,String category,int seats,boolean priv,String joinCode,String entranceMode,String entryEpoch' in create
assert 'private void retryRoomCreateLegacy921(DocumentReference ref,String name,String category,int seats,boolean priv,String joinCode,String entranceMode,String entryEpoch' in create
party_nav=party[party.index('private void addBottomNav('):party.index('    private void reducePartyMemory',party.index('private void addBottomNav('))]
assert 'KingNav.openRoot(this,tab);' in party_nav
nav=(java/'KingNav.java').read_text()
assert 'if (tab == 0 || tab == 1)' in nav
assert 'ROOT_IDS = {0, 2, 3, 4, 5}' in nav
assert 'ROOT_NAMES = {"Home", "Games", "Discover", "Messages", "Me"}' in nav
for name in ('MainActivity.java','PartyActivity.java','KingParityHubActivity.java','DiscoverActivity.java','InboxActivity.java'):
    source=(java/name).read_text()
    assert 'KingNav.bottomBar(' in source and 'KingNav.NAV_HEIGHT_DP' in source
assert 'homeRoomSearch=q;' in hub and 'matchesHomeRoom(doc)' in hub
assert 'whereEqualTo("joinCode",code).limit(10).get(Source.SERVER)' in hub
assert 'private void openPartyLobby(){\n        homeRoomSearch=' in hub
assert 'else if (tab == 2)' in nav
assert 'i.putExtra("openTab", 1);' in nav
gradle=(root/'app/build.gradle').read_text()
policy=party.split("private boolean canConnectPartyVoice()",1)[1].split("private void showPartyVoiceUnavailable()",1)[0]
assert "roomVoicePolicyVerified" in policy and "cloudflareVoiceReady10066" in policy and "!roomPrivate&&!roomHasPassword" in policy
assert 'getHttpsCallable("partyVoice").call(data)' in party
assert 'data.put("action","config")' in party
assert 'checkCloudflareVoiceConfig10066(expectedRoom982,expectedUid982,visit982,binding982,false);' in party
assert "BuildConfig.CLOUDFLARE_VOICE_ENABLED" not in party and "kingPartyCloudflareVoice" not in gradle
for entry in ("private void showVoiceConnection10065()", "private void attachInRoomVoice940(FrameLayout shell)"):
    voice=party.split(entry,1)[1]
    assert voice.index("if(!canConnectPartyVoice())") < voice.index("if(cloudflareVoiceReady10066)")
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


# Home subscriptions must be owned by the visible Activity and current room list.
start=hub.split("@Override protected void onStart(){",1)[1].split("@Override protected void onStop(){",1)[0]
stop=hub.split("@Override protected void onStop(){",1)[1].split("@Override protected void onDestroy(){",1)[0]
destroy=hub.split("@Override protected void onDestroy(){",1)[1].split("private LinearLayout rootNav()",1)[0]
assert "homeForeground=true;" in start and "loadHomeLiveRooms(activeHomeRoomRows)" in start
assert "homeRoomCursor=null;" in start and "homeRoomPrevious.clear();" in start
assert "homeForeground=false;" in stop and "++homeRoomRequest;" in stop and "stopHomeRoomListener();" in stop
assert "stopHomeRoomListener();" in destroy and "activeHomeRoomRows=null;" in destroy
assert "rows==activeHomeRoomRows" in hub and "return homeForeground&&!isFinishing()" in hub
assert 'KingRoomInvite959.parse(q)' in hub and 'i.putExtra("directRoomId",invite)' in hub
print("PASS Home lifecycle subscription ownership and explicit Party invite routing source guards")

# Unified golden Home room discovery layout: real Firebase covers, two equal columns and one room entry route.
assert 'HOME_GRID_ROW' in hub and 'row.addView(item,cell)' in hub
assert 'new LinearLayout.LayoutParams(0,-2,1)' in hub and 'empty.setTag("empty")' in hub
assert 'addHomeWelcomeBanner();' in home and 'openSavedHomeRooms()' in hub
assert 'homeAction("Join ID / Link",this::showHomeJoin)' in home
assert 'sectionTabs.setVisibility("home".equals(r)?View.GONE:View.VISIBLE)' in hub
assert 'addSnapshotListener(MetadataChanges.INCLUDE' in hub
assert 'openHomeRoom(doc.getId())' in hub and 'homeUnorderedRooms=true;homeRoomCursor=null;homeRoomPrevious.clear();' in hub
print("PASS golden Home grid, saved rooms, direct entry and metadata-aware live room pagination")

# Reference-video-inspired compact Party sheets preserve the existing real send methods.
emoji_sheet=party.split("private void showLiveEmojiPanelV530()",1)[1].split("private void previewLockedAction993",1)[0]
gift_sheet=party.split("private void giftShopPanel()",1)[1].split("private void rebuildGiftCategories867()",1)[0]
assert 'group==2||group==3?6:4' in emoji_sheet and 'sheet.setBackgroundColor(0xfffffdf8)' in emoji_sheet
assert 'sendMessage(composerBox)' in emoji_sheet and 'fillEmojiGrid610' in emoji_sheet
assert 'showCompactGiftTools()' in gift_sheet and 'sendPaidPartyGift10064()' in gift_sheet
assert 'giftCategory="Activity"' in gift_sheet and 'root.addView(giftSelectionLabel' in gift_sheet
assert 'giftVipRequired[giftSelectedIndex]>0' in gift_sheet
print("PASS compact Party emoji and gifting sheets, existing server send and locked-gift guards")

# Party controls and today's bounded server statistics must not overwrite entitlements or stack template sheets.
assert 'if(sheet[0]!=null)sheet[0].dismiss();activityCenterPanel(tab)' in party
assert '"Treasure Hunt","Chicken Run"' in party
assert 'if(name.contains("(external)"))' in party
assert 'labels.add("⚙ Room management");actions.add(this::moderationPanel)' in party
assert 'case 1:inviteDialog();break;' in party
assert 'whereGreaterThanOrEqualTo("createdAt",since)' in party and 'Unique gift senders' in party
assert 'Latest 200 events only' in party and 'Recorded paid gift value' in party
wardrobe=(java/'KingWardrobeActivity.java').read_text()
assert 'm.put("vipLevel"' not in wardrobe and 'm.put("vipPoints"' not in wardrobe
assert 'Cloud sync failed. Previous equipped item retained.' in wardrobe
print('PASS Party direct controls, bounded current-day room stats and entitlement-safe cosmetic sync')

assert 'getHttpsCallable("getDailyCheckinStatus").call()' in wardrobe
assert 'm.put(field,name)' in wardrobe and 'obtainedOnly&&!unlocked' in wardrobe
assert 'progress=new LevelSystem.Snapshot(Math.max(1,Math.min(99,level)),xp,0,0)' in wardrobe
public=(java/'KingPublicProfileActivity.java').read_text()
assert 'Public medals and purchase ownership are not verified here.' in public
assert 'No verified public prestige record' in public
print('PASS account-backed privilege browsing, guarded equip and truthful public collection')
