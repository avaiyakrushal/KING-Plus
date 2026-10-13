#!/usr/bin/env python3
"""KING Plus v9.9.61: persist Host's own Werewolf/Spy private role.
Real Party gameplay: rotate/re-enter no longer relies only on transient Host Activity fields.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]); src=root/'app/src/main/java/com/kingplus/social'
file=src/'RoomGameActivity.java'; text=file.read_text()
a=text.index('    private void startSocialRound866(String game){')
b=text.index('    private void startRound(String game){',a)
social=text[a:b]
old='''                        if(uid9958.equals(hostUid9958)){
                            hostRole9958=role9958;hostSecret9958=personal9958;
                            continue;
                        }
                        java.util.Map<String,Object> invite9958=new HashMap<>();'''
new='''                        if(uid9958.equals(hostUid9958)){
                            // The Host is a real participant too. Cache for immediate UI,
                            // but DO NOT skip their private recipient-only Firestore invite.
                            // This is needed when Android re-creates the Host Activity.
                            hostRole9958=role9958;hostSecret9958=personal9958;
                        }
                        java.util.Map<String,Object> invite9958=new HashMap<>();'''
assert social.count(old)==1,'Expected complete verified v9.9.60 social game role loop'
social=social.replace(old,new,1)
assert 'roleDocId866(next9958,uid9958)' in social
assert 'tx.set(room().collection("room_invites")' in social
assert 'KingPartySocialRoster9959.isEligible' in social
assert 'KingTttFairRoster9960' in text
assert 'liveUids9958.contains(hostUid9958)' in social
file.write_text(text[:a]+social+text[b:])
g=root/'app/build.gradle'; gradle=g.read_text()
oldver="versionCode 239; versionName '9.9.60-fair-live-ttt-ready-test'"
assert gradle.count(oldver)==1,'Must use full 9.9.60 APK source'
g.write_text(gradle.replace(oldver,
    "versionCode 240; versionName '9.9.61-persistent-host-private-game-role-test'"))
party=(src/'PartyActivity.java').read_text()
for cls in ('KingPartyVoiceReconnect9957','KingPartyMicConsent9956','KingGiftReceiptRetry9955'):
    assert cls in party,cls
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS Host private role stored with other verified Party participant roles, original RTC/Gift intact')
