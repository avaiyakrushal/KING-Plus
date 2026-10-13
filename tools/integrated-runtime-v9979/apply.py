#!/usr/bin/env python3
"""Integrated KING Plus v9.9.79: real 2-phone game sync and score integrity.

Input: complete signed v9.9.78 Android source archive, NOT stale main branch.
Patches functional Firestore listening and fraud-resistance. Preserves Gift,
Voice/Mic, and no-billing protection; never changes XML/Drawable counts.
"""
from pathlib import Path
import re,sys
root=Path(sys.argv[1])
base=root/'app/src/main/java/com/kingplus/social'
game=base/'RoomGameActivity.java'
s=game.read_text()
old='''    private ListenerRegistration stateListener,movesListener,readyListener,moderatorRoleListener;'''
new='''    private ListenerRegistration stateListener,movesListener,readyListener,moderatorRoleListener;
    // A state metadata update is NOT a new round. Re-subscribing game_moves
    // on every snapshot drops live score state and can restart result checks.
    private long observedMoveRound9979=-1L;
    private String observedMoveType9979="";'''
assert s.count(old)==1,'Expected original v9.9.78 RoomGameActivity listener declaration'
s=s.replace(old,new,1)
old='''    private void listenMoves(){
        if(movesListener!=null){movesListener.remove();movesListener=null;}
        currentMoves.clear();'''
new='''    private void listenMoves(){
        if(!KingPartyMoveSubscription9979.needsNewListener(movesListener!=null,
              observedMoveRound9979,observedMoveType9979,roundId,type)){
            // Snapshot was just a server timestamp/metadata/result change.
            // Keep the existing Firebase stream and the moves already shown
            // to the other phone; renderState()/renderControls() still run.
            return;
        }
        if(movesListener!=null){movesListener.remove();movesListener=null;}
        observedMoveRound9979=-1L;observedMoveType9979="";
        currentMoves.clear();'''
assert s.count(old)==1,'Expected exact v9.9.78 live-round listener'
s=s.replace(old,new,1)
old='''        final long observingRound9977=roundId;
        movesListener=room().collection("game_moves")'''
new='''        final long observingRound9977=roundId;
        observedMoveRound9979=observingRound9977;
        observedMoveType9979=type;
        movesListener=room().collection("game_moves")'''
assert s.count(old)==1,'Expected only one current-round scoped listener'
s=s.replace(old,new,1)
game.write_text(s)
(base/'KingPartyMoveSubscription9979.java').write_text('''package com.kingplus.social;
import java.util.Objects;
/** Replace room-game Firestore move listener only for actual match identity changes. */
public final class KingPartyMoveSubscription9979 {
  private KingPartyMoveSubscription9979() {}
  public static boolean needsNewListener(boolean attached,long observedRound,
                                            String observedType,long currentRound,String currentType){
    return !attached || observedRound!=currentRound ||
      !Objects.equals(observedType,currentType);
  }
}
''')
rules=root/'firestore.rules';r=rules.read_text()
patches=[
("""&& request.resource.data.move.matches('^Spin ([1-9]|[1-9][0-9]|100)$')))""",
 """&& request.resource.data.move.matches('^Spin ([1-9]|[1-9][0-9]|100)$')
                  && request.resource.data.move == 'Spin ' + string(request.resource.data.score)))"""),
("""&& request.resource.data.move.matches('^([1-9]|[12][0-9]|30)$')))""",
 """&& request.resource.data.move.matches('^([1-9]|[12][0-9]|30)$')
                  && request.resource.data.move == string(request.resource.data.score)))"""),
("""&& request.resource.data.move.matches('^Pick [1-9]$')))""",
 """&& request.resource.data.move.matches('^Pick [1-9]$')
                  && request.resource.data.move == 'Pick ' + string(request.resource.data.score)))"""),
("""&& request.resource.data.move.matches('^.+ power ([2-9][0-9]|100)$')))""",
 """&& request.resource.data.move.matches('^.+ power ([2-9][0-9]|100)$')
                  && request.resource.data.move.matches(
                      '^(🦁 Lion|🐯 Tiger|🐼 Panda) power ' +
                      string(request.resource.data.score) + '$')))"""),
("""&& request.resource.data.score >= 1
                      && request.resource.data.score <= 99999
                      && request.resource.data.get('move','') is string
                      && request.resource.data.move.matches('^Reaction [0-9]{1,5} ms$'))))""",
 """&& request.resource.data.score >= 70000
                      && request.resource.data.score <= 99999
                      && request.resource.data.get('move','') is string
                      && request.resource.data.move.matches('^Reaction [0-9]{1,5} ms$')
                      && request.resource.data.move ==
                         'Reaction ' + string(100000 - request.resource.data.score) + ' ms'))))""")
]
for a,b in patches:
    assert r.count(a)==1, 'Firestore rules score validation anchor absent: '+a[:45]
    r=r.replace(a,b,1)
rules.write_text(r)
gradle=root/'app/build.gradle';g=gradle.read_text()
g,n=re.subn(r"versionCode\s+257\s*;\s*versionName\s+'[^']+'",
 "versionCode 258; versionName '9.9.79-real-score-consistency-live-sync-test'",g)
assert n==1,'Expect v9.9.78 signed Android base versionCode257'
gradle.write_text(g)
assert 'whereEqualTo("roundId",observingRound9977)' in s
assert 'KingPartyLiveRoster9975.member' in s
assert 'KingPartyMoveCleanup9976.roster' in s
assert 'LIVE_RECHARGE = false' in (base/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (base/'KingPremiumAccess981.java').read_text()
print('PASS real game live Firestore subscription retained on metadata updates and forged move labels refused')
