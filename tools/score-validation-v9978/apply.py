#!/usr/bin/env python3
"""v9.9.78: Bound actual multiplayer moves in Firestore Security Rules.

Correct-looking Android controls are NOT enough. A modified client may write
score=999999999 and steal a multiplayer win while its display merely says
Spin 6. This rules hardening keeps honest UI moves working and rejects extreme
fabrications for Wheel/Slot/Bingo/Number/HighLow/Zoo/Reaction.
No billing is enabled; trusted hosted game RNG is future work.
"""
from pathlib import Path
import re,sys
root=Path(sys.argv[1])
file=root/'firestore.rules'
rules=file.read_text()
anchor='''          // Sheep cannot disguise a move as Dice/RPS to bypass the round timer.'''
assert rules.count(anchor)==1,'Unexpected v9.9.77 rules structure'
hardening='''          // Generic Party games formerly accepted unbounded fabricated client
          // scores (e.g. Wheel 999999999). These rules bind each game to
          // possible values and the actual move-label format.
          // IMPORTANT: This is input integrity, NOT trusted RNG/anti-cheat.
          && (request.resource.data.type != 'wheel'
              || (request.resource.data.get('score',-1) is int
                  && request.resource.data.score >= 1
                  && request.resource.data.score <= 100
                  && request.resource.data.get('move','') is string
                  && request.resource.data.move.matches('^Spin ([1-9]|[1-9][0-9]|100)$')))
          && (request.resource.data.type != 'number'
              || (request.resource.data.get('score',-1) is int
                  && request.resource.data.score >= 1
                  && request.resource.data.score <= 30
                  && request.resource.data.get('move','') is string
                  && request.resource.data.move.matches('^([1-9]|[12][0-9]|30)$')))
          && (request.resource.data.type != 'bingo'
              || (request.resource.data.get('score',-1) is int
                  && request.resource.data.score >= 1
                  && request.resource.data.score <= 9
                  && request.resource.data.get('move','') is string
                  && request.resource.data.move.matches('^Pick [1-9]$')))
          && (request.resource.data.type != 'highlow'
              || ((request.resource.data.get('move','') == 'High'
                     && request.resource.data.get('score',-1) == 1)
                  || (request.resource.data.get('move','') == 'Low'
                     && request.resource.data.get('score',-1) == 0)))
          && (request.resource.data.type != 'slot'
              || (request.resource.data.get('score',-1) is int
                  && request.resource.data.score >= 0
                  && request.resource.data.score <= 77
                  && request.resource.data.get('move','') is string
                  && request.resource.data.move.matches('^Spin [0-9]-[0-9]-[0-9]$')))
          && (request.resource.data.type != 'zoo'
              || (request.resource.data.get('score',-1) is int
                  && request.resource.data.score >= 25
                  && request.resource.data.score <= 100
                  && request.resource.data.get('move','') is string
                  && request.resource.data.move.matches('^.+ power ([2-9][0-9]|100)$')))
          && (request.resource.data.type != 'reaction'
              || ((request.resource.data.get('score',-1) == 0
                     && request.resource.data.get('move','') in
                         ['False start before GO','Reaction expired'])
                  || (request.resource.data.get('score',-1) is int
                      && request.resource.data.score >= 1
                      && request.resource.data.score <= 99999
                      && request.resource.data.get('move','') is string
                      && request.resource.data.move.matches('^Reaction [0-9]{1,5} ms$'))))
'''
rules=rules.replace(anchor,hardening+anchor,1)
file.write_text(rules)
gradle=root/'app/build.gradle';g=gradle.read_text()
g,n=re.subn(r"versionCode\s+256\s*;\s*versionName\s+'[^']+'",
 "versionCode 257; versionName '9.9.78-valid-real-game-scores-test'",g)
assert n==1,'Only apply to signed full v9.9.77'
gradle.write_text(g)
gamedata=(root/'app/src/main/java/com/kingplus/social/RoomGameActivity.java').read_text()
for evidence in ('KingPartyMoveCleanup9976.roster','KingPartyLiveRoster9975.member',
                 'KingPartyAllPlayed9973.complete','whereEqualTo("roundId",observingRound9977)'):
 assert evidence in gamedata,evidence
party=(root/'app/src/main/java/com/kingplus/social/PartyActivity.java').read_text()
assert 'KingGiftPlayedHistory9969' in party
assert 'KingGiftSound993.stopRoomAudio();' in party
assert 'LIVE_RECHARGE = false' in (root/'app/src/main/java/com/kingplus/social/KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (root/'app/src/main/java/com/kingplus/social/KingPremiumAccess981.java').read_text()
print('PASS 7 actual Party game types validated at Firestore boundary; Gifts/RTC/wallet untouched')
